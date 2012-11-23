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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import juzu.plugin.shiro.realm.JuzuShiroRealmHandle;
import juzu.plugin.shiro.realm.UserHandle;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class SimpleJuzuShiroRealmHandle implements JuzuShiroRealmHandle
{

   private Map<String, UserHandle> handlers = new HashMap<String, UserHandle>();
   
   public SimpleJuzuShiroRealmHandle()
   {
      UserHandle userHandle = new SimpleUserHandle();
      handlers.put(userHandle.getName(), userHandle);
   }
   
   public Collection<UserHandle> getAllUserHandle()
   {
      return handlers.values();
   }
   
   public UserHandle getUserHandleByName(String name)
   {
      return handlers.get(name);
   }
}
