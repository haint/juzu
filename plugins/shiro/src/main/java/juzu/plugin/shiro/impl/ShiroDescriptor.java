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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import juzu.Response;
import juzu.impl.common.JSON;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.request.Request;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroDescriptor extends Descriptor
{
   private final JSON config;
   
   ShiroDescriptor(JSON config)
   {
      Ini ini = new Ini();
      
      //
      List<JSON> users = (List<JSON>)config.getList("users");
      if(users != null && users.size() > 0)
      {
         Section section = ini.addSection("users");
         for(JSON user : users) 
         {
            StringBuilder sb = new StringBuilder();
            String username = (String)user.get("username");
            String password = (String)user.get("password");
            if(password != null && password.length() > 0)
            {
               sb.append(user.get("password"));
            }
            
            ArrayList<String> roles = (ArrayList<String>)user.get("roles");
            if( roles != null && roles.size() > 0)
            {
               sb.append(",");
               Iterator<String> i = roles.iterator();
               while(i.hasNext())
               {
                  sb.append(i.next());
                  if(i.hasNext())
                  {
                     sb.append(",");
                  }
               }
            }
            if(sb.length() > 0)
            {
               section.put(username, sb.toString());
            }
         }
      }
      
      //
      List<JSON> roles = (List<JSON>)config.getList("roles");
      if(roles != null && roles.size() > 0)
      {
         Section section = ini.addSection("roles");
         for(JSON role : roles) 
         {
            StringBuilder sb = new StringBuilder();
            String roleName = (String)role.get("name");
            ArrayList<String> permissions = (ArrayList<String>)role.get("permissions");
            if( permissions != null && permissions.size() > 0)
            {
               Iterator<String> i = permissions.iterator();
               while(i.hasNext())
               {
                  sb.append(i.next());
                  if(i.hasNext())
                  {
                     sb.append(",");
                  }
               }
            }
            
            if(sb.length() > 0)
            {
               section.put(roleName, sb.toString());
            }
         }
      }
      
      Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
      SecurityManager sm = factory.getInstance();
      SecurityUtils.setSecurityManager(sm);
      
      this.config = config;
   }
   
   public void invoke(Request request) throws ApplicationException
   {
      JSON json = config.getJSON("methods").getJSON(request.getContext().getMethod().getHandle().toString());
      if(json != null && isIntercepted(json))
      {
         System.out.println(request.getContext().getMethod().getHandle().toString() + " has been intercepted");
         request.setResponse(Response.content(401, "Unauthorization"));
      }
      else
      {
         request.invoke();
      }
   }
   
   private boolean isIntercepted(JSON json)
   {
      //
      if(json.get("guest") != null && json.getBoolean("guest"))
      {
         if(SecurityUtils.getSubject().getPrincipal() != null) 
         {
            return true;
         }
      }
      
      //
      if(json.get("user") != null && json.getBoolean("user"))
      {
         if(SecurityUtils.getSubject().getPrincipal() == null)
         {
            return true;
         }
      }
      
      //
      if(json.get("authenticate") != null && json.getBoolean("authenticate"))
      {
         if(SecurityUtils.getSubject().getPrincipal() == null)
         {
            return true;
         }
      }
      
      //
      if(json.get("role") != null)
      {
         JSON foo = json.getJSON("role");
         Logical  logical = Logical.valueOf(foo.getString("logical"));
         List<String> roles = (List<String>)foo.get("value");
         if(roles.size() == 1)
         {
            return SecurityUtils.getSubject().hasRole(roles.get(0));
         }
         else if(roles.size() > 1)
         {
            switch (logical)
            {
               case AND :
                  return SecurityUtils.getSubject().hasAllRoles(roles);
               case OR :
                  for(String role : roles)
                  {
                     if(SecurityUtils.getSubject().hasRole(role))
                     {
                        return false;
                     }
                  }
                  return true;
            }
         }
      }

      if(json.get("permission") != null)
      {
         JSON foo = json.getJSON("permission");
         Logical  logical = Logical.valueOf(foo.getString("logical"));
         List<String> permissions = (List<String>)foo.get("value");
          if(permissions.size() == 1)
          {
             return SecurityUtils.getSubject().isPermitted(permissions.get(0));
          }
          else if(permissions.size() > 1)
          {
             switch (logical)
             {
                case AND :
                   for(String permission : permissions)
                   {
                      if(!SecurityUtils.getSubject().isPermitted(permission))
                      {
                         return true;
                      }
                   }
                   return false;
                case OR :
                   for(String permission : permissions)
                   {
                      if(SecurityUtils.getSubject().isPermitted(permission))
                      {
                         return false;
                      }
                   }
                   return true;
             }
          }
      }
      
      return false;
   }
}
