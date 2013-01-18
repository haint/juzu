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
package juzu.plugin.shiro.common;

import java.util.Arrays;
import java.util.Collection;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroTools
{

   /**
    * Check has roles with OR logical
    * 
    * @param roles
    * @return true even if has one role
    */
   public static boolean hasRole(String... roles)
   {
      for(String role : roles)
      {
         if(SecurityUtils.getSubject().hasRole(role))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Check has permissions with OR logical
    * 
    * @param permissions
    * @return true even if has one permission
    */
   public static boolean isPermitted(String... permissions) 
   {
      for(String permission : permissions)
      {
         if(SecurityUtils.getSubject().isPermitted(permission))
         {
            return true;
         }
      }
      return false;
   }
   
   public static boolean containRealm(String realmName)
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
}
