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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

import juzu.Response;
import juzu.impl.common.JSON;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.request.Request;
import juzu.request.MimeContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroAuthenticater
{
   /** . */
   private final JSON config;
   
   /** . */
   private String loginFailedURL;
   
   public ShiroAuthenticater(JSON config)
   {
      this.config = config;
   }
   
   public Request invoke(Request request)
   {
      String loginFailedMethodId = config.getString("loginFailed");
      if(loginFailedURL == null && loginFailedMethodId != null)
      {
         List<MethodDescriptor> methods = request.getApplication().getDescriptor().getControllers().getMethods();
         for(MethodDescriptor method : methods)
         {
            if(method.getHandle().toString().equals(loginFailedMethodId))
            {
               loginFailedURL = ((MimeContext) request.getContext()).createURLBuilder(method).toString();
            }
         }
      }
      
      JSON json = config.getJSON("methods").getJSON(request.getContext().getMethod().getHandle().toString());
      if(json == null)
      {
         return request;
      }
      
      Subject subject = SecurityUtils.getSubject();
      if(subject.isAuthenticated())
      {
         if(json.get("logout") != null && json.getBoolean("logout"))
         {
            subject.logout();
            return request;
         }
         else
         {
            return request;
         }
      }
      
      if(json.get("login") != null && json.getBoolean("login"))
      {
         String username = request.getParameters().get(json.getString("username"))[0];
         String password = request.getParameters().get(json.getString("password"))[0];
         try
         {
            subject.login(new UsernamePasswordToken(username, password.toCharArray()));
            return request;
         }
         catch (AuthenticationException e)
         {
            if(loginFailedURL == null)
            {
               request.setResponse(Response.content(401, "Unauthorized"));
            }
            else
            {
               request.setResponse(Response.redirect(loginFailedURL));
            }
            return null;
         }
      }
      
      return request;
   }
}
