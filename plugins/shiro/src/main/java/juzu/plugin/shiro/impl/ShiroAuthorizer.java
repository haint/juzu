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

import juzu.Response;
import juzu.impl.common.JSON;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.Parameter;
import juzu.impl.request.Request;
import juzu.plugin.shiro.common.ShiroTools;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroAuthorizer
{
   public boolean doAuthorize(Request request, JSON json)
   {
      if(isAuthorized(request, json))
      {
         return true;
      }
      else
      {
         List<Parameter> parameters = request.getContext().getMethod().getParameters();
         for(Parameter parameter : parameters)
         {
            if(parameter instanceof ContextualParameter && parameter.getType().equals(AuthorizationException.class)) 
            {
               AuthorizationException.class.isAssignableFrom(parameter.getType());
               request.setArgument(parameter, new AuthorizationException());
               request.invoke();
               return false;
            }
         }
         request.setResponse(Response.content(401, "Unauthorized"));
         return false;
      }
   }
   
   private boolean hasRequire(Request request, JSON config) 
   {
      Object obj = config.get("require");
      if("guest".equals(obj))
      {
         return SecurityUtils.getSubject().getPrincipal() == null;
      }
      else if("authenticate".equals(obj))
      {
         return SecurityUtils.getSubject().isAuthenticated();
      }
      else if("user".equals(obj))
      {
         return SecurityUtils.getSubject().getPrincipal() != null;
      }
      
      return false;
   }
   
   private boolean hasRoles(Request request, JSON config)
   {
      if(!SecurityUtils.getSubject().isAuthenticated())
      {
         return false;
      }

      JSON foo = config.getJSON("roles");
      Logical  logical = Logical.valueOf(foo.getString("logical"));
      List<String> roles = (List<String>)foo.get("value");
      if(roles.size() == 1)
      {
         return ShiroTools.hasRole(roles.get(0));
      }
      else if(roles.size() > 1)
      {
         switch (logical)
         {
            case AND :
               return SecurityUtils.getSubject().hasAllRoles(roles);
            case OR :
               return ShiroTools.hasRole(roles.toArray(new String[roles.size()]));
         }
      }
      return false;
   }
   
   private boolean hasPermissions(Request request, JSON config)
   {
      if(!SecurityUtils.getSubject().isAuthenticated())
      {
         return false;
      }

      JSON foo = config.getJSON("permissions");
      Logical  logical = Logical.valueOf(foo.getString("logical"));
      List<String> permissions = (List<String>)foo.get("value");
      if(permissions.size() == 1)
      {
         return ShiroTools.isPermitted(permissions.get(0));
      }
      else if(permissions.size() > 1)
      {
         switch (logical)
         {
            case AND :
               return SecurityUtils.getSubject().isPermittedAll(permissions.toArray(new String[permissions.size()]));
            case OR :
               return ShiroTools.isPermitted(permissions.toArray(new String[permissions.size()]));
         }
      }
      return false;
   }

   private boolean isAuthorized(Request request, JSON config)
   {
      if(config.get("require") != null)
      {
         return hasRequire(request, config);
      }
      
      if(config.get("roles") != null && config.get("permissions") != null)
      {
         return hasRoles(request, config) && hasPermissions(request, config);
      }

      if(config.get("permissions") != null && config.get("roles") == null)
      {
         return hasPermissions(request, config);
      }
      
      if(config.get("roles") != null && config.get("permissions") == null)
      {
         return hasRoles(request, config);
      }

      return true;
   }
}
