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
package org.sample.shiro.realm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import juzu.impl.common.Tools;
import juzu.plugin.shiro.mgt.UserHandle;
import juzu.plugin.shiro.mgt.UserInfo;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class SimpleUserHandle implements UserHandle
{
   private Map<String, UserInfo> users = new HashMap<String, UserInfo>();
   
   private Map<String, Set<String>> roles = new HashMap<String, Set<String>>();
   
   private Map<String, Set<String>> permissions = new HashMap<String, Set<String>>();
   
   public SimpleUserHandle()
   {
      users.put("root", new UserInfo("root", "secret"));
      roles.put("root", Tools.set("admin"));
      
      users.put("guest", new UserInfo("guest", "guest"));
      roles.put("guest", Tools.set("guest"));
      
      users.put("presidentskroob", new UserInfo("presidentskroob", "12345"));
      roles.put("presidentskroob", Tools.set("president"));
      
      users.put("darkhelmet", new UserInfo("darkhelmet", "ludicrousspeed"));
      roles.put("darkhelmet", Tools.set("darklord", "schwartz"));
      
      users.put("lonestarr", new UserInfo("lonestarr", "vespa"));
      roles.put("lonestarr", Tools.set("goodguy", "schwartz"));
      
      permissions.put("admin", Tools.set("*"));
      permissions.put("schwartz", Tools.set("lightsaber:*"));
      permissions.put("goodguy", Tools.set("winnebago:drive:eagle5"));
   }

   @Override
   public String getName()
   {
      return "simple";
   }

   @Override
   public UserInfo findUser(String username, String password)
   {
      return users.get(username);
   }

   @Override
   public Set<String> getRoles(String username)
   {
      return roles.get(username);
   }

   @Override
   public Set<String> getPermissions(String username, String role)
   {
      return permissions.get(role);
   }
}
