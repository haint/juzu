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
package plugin.shiro.authz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import juzu.impl.common.Builder;
import juzu.impl.common.Tools;
import juzu.plugin.shiro.mgt.UserInfo;
import juzu.plugin.shiro.mgt.UserHandle;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class SimpleUserHandle implements UserHandle
{
   /** .  */
   private final Map<String, UserInfo> users = new HashMap<String, UserInfo>();
   
   /** . */
   private final Map<String, Set<String>> roles = new HashMap<String, Set<String>>();
   
   /** . */
   private final Map<String, Map<String, Set<String>>> permissions = new HashMap<String, Map<String, Set<String>>>();
   
   public SimpleUserHandle()
   {
      users.put("root", new UserInfo("root", "secret"));
      users.put("haint", new UserInfo("haint", "haint"));
      
      roles.put("root", Tools.set("role1", "role2"));
      roles.put("haint", Tools.set("role2"));
      
      permissions.put("root", Builder.<String, Set<String>>map("role1", Tools.set("test1", "test2")).build());
      permissions.put("haint", Builder.<String, Set<String>>map("role2", Tools.set("test2")).build());
   }
   
   public String getName()
   {
      return "inMemory";
   }

   public UserInfo findUser(String username, String password)
   {
      return users.get(username);
   }

   public Set<String> getRoles(String username)
   {
      return roles.get(username);
   }

   public Set<String> getPermissions(String username, String role)
   {
      Map<String, Set<String>> foo = permissions.get(username);
      if(foo == null) return null;
      return foo.get(role);
   }
}
