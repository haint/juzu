/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package juzu.shiro.plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import juzu.impl.common.JSON;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.request.Request;
import juzu.shiro.Supported;
import juzu.shiro.impl.JuzuRememberMe;
import juzu.shiro.impl.SecurityManagerScoped;
import juzu.shiro.impl.ShiroAuthenticator;
import juzu.shiro.impl.ShiroAuthorizor;
import juzu.shiro.impl.SubjectScoped;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroDescriptor extends Descriptor
{
   /** . */
   private final ShiroAuthorizor authorizer;
   
   /** . */
   private final ShiroAuthenticator authenticater;
   
   /** . */
   private final JSON config;
   
   /** . */
   private boolean rememberMeSupported = false;
   
   ShiroDescriptor(JSON config)
   {
      List<String> supports = (List<String>)config.getList("supports");
      if(supports != null)
      {
         this.rememberMeSupported = supports.contains(Supported.rememberMe.toString()) ? true : false;
      }
      
      this.authenticater = new ShiroAuthenticator(rememberMeSupported);
      this.authorizer = new ShiroAuthorizor();
      this.config = config;
   }
   
   private void start(Request request) throws InvocationTargetException
   {
      org.apache.shiro.mgt.SecurityManager currentManager = null;
      try
      {
         currentManager = SecurityUtils.getSecurityManager();
      }
      catch (UnavailableSecurityManagerException e)
      {
         if(request.getBridge().getSessionValue("currentManager") != null)
         {
            currentManager = (DefaultSecurityManager)request.getBridge().getSessionValue("currentManager").get();
         }
         else
         {
            currentManager = new DefaultSecurityManager();
            ((DefaultSecurityManager)currentManager).setRememberMeManager(rememberMeSupported ? new JuzuRememberMe() : null);
            SecurityManagerScoped value = new SecurityManagerScoped(currentManager);
            request.getBridge().setSessionValue("currentManager", value);
         }
      }
      
      BeanLifeCycle bean = request.getApplication().getInjectionContext().get(AuthorizingRealm.class);
      if(bean != null)
      {
         Realm realm = (Realm)bean.get();
         Collection<Realm> realms = ((DefaultSecurityManager)currentManager).getRealms();
         if(realms == null)
         {
            ((DefaultSecurityManager)currentManager).setRealm(realm);
         }
         else
         {
            boolean notExisted = false;
            for(Realm sel : realms)
            {
               if(sel.getName().equals(realm.getName()))
               {
                  notExisted = true;
                  break;
               }
            }
            if(notExisted) ((DefaultSecurityManager)currentManager).getRealms().add(realm);
         }
      }

      Subject currentUser = null;
      if(request.getBridge().getSessionValue("currentUser") != null)
      {
         currentUser = (Subject)request.getBridge().getSessionValue("currentUser").get();
      }
      else
      {
         Subject.Builder builder = new Subject.Builder(currentManager);
         currentUser = builder.buildSubject();
         SubjectScoped value = new SubjectScoped(currentUser);
         request.getBridge().setSessionValue("currentUser", value);
      }

      //
      ThreadContext.bind(currentUser);
      ThreadContext.bind(currentManager);
   }

   private void end()
   {
      ThreadContext.unbindSubject();
      ThreadContext.unbindSecurityManager();
   }
   
   public void invoke(Request request) throws ApplicationException, InvocationTargetException
   {
      try
      {
         //
         start(request);
         
         //
         String methodId = request.getContext().getMethod().getHandle().toString();
         String controllerId = methodId.substring(0, methodId.indexOf('#'));
         methodId = methodId.substring(controllerId.length() + 1);
         JSON controllerJSON = config.getJSON(controllerId);
         if(controllerJSON == null)
         {
            request.invoke();
         }
         
         //
         if(controllerJSON.get("require") != null)
         {
            authorizer.isAuthorized(request, controllerJSON);
         }
         else
         {
            JSON methodsJSON = controllerJSON.getJSON("methods");
            if(methodsJSON != null)
            {
               JSON json = methodsJSON.getJSON(methodId);
               
               if(json != null)
               {
                  if(authorizer.isAuthorized(request, json))
                  {
                     if("login".equals(json.get("operator")))
                     {
                        authenticater.doLogin(request);
                     }
                     else if("logout".equals(json.get("operator")))
                     {
                        authenticater.doLogout(request);
                     }
                     else
                     {
                        request.invoke();
                     }
                  }
               }
               else
               {
                  request.invoke();
               }
            }
            else
            {
               request.invoke();
            }
         }
      } 
      finally
      {
         end();
      }
   }
}
