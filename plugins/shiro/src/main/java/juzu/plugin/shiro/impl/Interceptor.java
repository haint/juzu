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

import juzu.impl.common.JSON;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class Interceptor
{

   private final JSON config;

   Interceptor(JSON config)
   {
      this.config = config;
      init();
   }

   private void init()
   {
      Ini ini = new Ini();
      List<JSON> realms = (List<JSON>)config.getList("realms");
      if(realms != null && realms.size() > 0)
      {
         if(realms.size() == 1)
         {
            JSON realm = realms.get(0);
            String name = realm.getString("name");
            String value = realm.getString("value");
            ini.setSectionProperty("main", name, value);
         }
      }

      Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
      SecurityManager sm = factory.getInstance();
      SecurityUtils.setSecurityManager(sm);
   }

   public boolean allow(String methodId)
   {
      JSON json = config.getJSON("methods").getJSON(methodId);
      if(json == null)
      {
         return true;
      }
      
      //
      if(json.get("guest") != null && json.getBoolean("guest"))
      {
         if(SecurityUtils.getSubject().getPrincipal() != null) 
         {
            return false;
         }
      }

      //
      if(json.get("user") != null && json.getBoolean("user"))
      {
         if(SecurityUtils.getSubject().getPrincipal() == null)
         {
            return false;
         }
      }

      //
      if(json.get("authenticate") != null && json.getBoolean("authenticate"))
      {
         return SecurityUtils.getSubject().isAuthenticated();
      }

      //
      if(json.get("role") != null)
      {
         if(!SecurityUtils.getSubject().isAuthenticated())
         {
            return false;
         }

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
                        return true;
                     }
                  }
                  return false;
            }
         }
      }

      if(json.get("permission") != null)
      {
         if(!SecurityUtils.getSubject().isAuthenticated())
         {
            return false;
         }

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
                  return SecurityUtils.getSubject().isPermittedAll(permissions.toArray(new String[permissions.size()]));
               case OR :
                  for(String permission : permissions)
                  {
                     if(SecurityUtils.getSubject().isPermitted(permission))
                     {
                        return true;
                     }
                  }
                  return false;
            }
         }
      }

      return true;
   }
}
