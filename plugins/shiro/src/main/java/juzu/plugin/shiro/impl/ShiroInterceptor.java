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

import java.util.Collection;
import java.util.List;

import juzu.impl.common.JSON;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.request.Request;
import juzu.plugin.shiro.common.JuzuShiroTools;
import juzu.plugin.shiro.realm.JuzuShiroRealm;
import juzu.plugin.shiro.realm.JuzuShiroRealmHandle;
import juzu.plugin.shiro.realm.UserHandle;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroInterceptor
{

   private JSON config;
   
   ShiroInterceptor(JSON config)
   {
      this.config = config;
      init();
   }

   private void init()
   {
      DefaultSecurityManager sm = new DefaultSecurityManager();
      SecurityUtils.setSecurityManager(sm);
   }
   
   private boolean containRealm(String realmName)
   {
      DefaultSecurityManager sm = (DefaultSecurityManager)SecurityUtils.getSecurityManager();
      Collection<Realm> realms = sm.getRealms();
      if(realms == null || realms.size() == 0)
      {
         return false;
      }
      for(Realm realm : realms)
      {
         if(realm.getName().equals(realmName)) return true;
      }
      return false;
   }

   public boolean allow(Request request) throws Exception
   {
      BeanLifeCycle bean =request.getApplication().getInjectionContext().get(JuzuShiroRealmHandle.class);
      if(bean != null)
      {
         DefaultSecurityManager sm = (DefaultSecurityManager)SecurityUtils.getSecurityManager();
         JuzuShiroRealmHandle handle = (JuzuShiroRealmHandle)bean.get();
         Collection<UserHandle> userHanlders = handle.getAllUserHandle();
         for(UserHandle userHandle : userHanlders)
         {
            if(!containRealm(userHandle.getName())) 
             {
                sm.setRealm(new JuzuShiroRealm(userHandle));
             }
         }
      }

      JSON json = config.getJSON("methods").getJSON(request.getContext().getMethod().getHandle().toString());
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
            return JuzuShiroTools.hasRole(roles.get(0));
         }
         else if(roles.size() > 1)
         {
            switch (logical)
            {
               case AND :
                  return JuzuShiroTools.hasAllRoles(roles.toArray(new String[roles.size()]));
               case OR :
                  return JuzuShiroTools.hasRole(roles.toArray(new String[roles.size()]));
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
            return JuzuShiroTools.isPermitted(permissions.get(0));
         }
         else if(permissions.size() > 1)
         {
            switch (logical)
            {
               case AND :
                  return JuzuShiroTools.isPermittedAll(permissions.toArray(new String[permissions.size()]));
               case OR :
                  return JuzuShiroTools.isPermitted(permissions.toArray(new String[permissions.size()]));
            }
         }
      }

      return true;
   }
}
