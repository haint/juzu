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
package juzu.plugin.shiro.impl;

import java.util.List;

import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Parameter;
import juzu.impl.request.Request;
import juzu.plugin.shiro.Login;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroAuthenticater
{
   
   private final boolean rememberMe;
   
   public ShiroAuthenticater(boolean rememberMe)
   {
      this.rememberMe = rememberMe;
   }

   public void doLogout(Request request)
   {
      SecurityUtils.getSubject().logout();
      request.invoke();
      if(rememberMe)
      {
         RememberMeUtil.forgetIdentity();
      }
   }

   public void doLogin(Request request)
   {
      Login loginAnnotation = request.getContext().getMethod().getMethod().getAnnotation(Login.class);
      Subject subject = SecurityUtils.getSubject();
      
      String susername = request.getParameters().get(loginAnnotation.username())[0];
      String spassword = request.getParameters().get(loginAnnotation.password())[0];
      boolean remember = request.getParameters().get(loginAnnotation.rememberMe()) != null ? true : false;
      try
      {
         subject.login(new UsernamePasswordToken(susername, spassword.toCharArray(), remember));
        
         //
         request.invoke();
         if(remember)
         {
            RememberMeUtil.forgetIdentity();
            RememberMeUtil.rememberSerialized(request.getResponse());
         }
      } 
      catch (AuthenticationException e)
      {
         List<Parameter> parameters = request.getContext().getMethod().getParameters();
         for(Parameter parameter : parameters) 
         {
            if(parameter instanceof ContextualParameter && parameter.getType().equals(AuthenticationException.class)) 
            {
               AuthenticationException.class.isAssignableFrom(parameter.getType());
               request.setArgument(parameter, e);
            }
         }
         request.invoke();
         if(remember)
         {
            RememberMeUtil.forgetIdentity();
         }
      }
   }
}
