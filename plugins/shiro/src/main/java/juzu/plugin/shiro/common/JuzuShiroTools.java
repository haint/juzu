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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 * This is a tool to wrap the Shiro Subject for decouple between the business code with the Shiro core
 */
public class JuzuShiroTools
{

   /**
    * 
    * @return the current principal of subject
    */
   public static String getCurrentUser()
   {
      return (String)SecurityUtils.getSubject().getPrincipal();
   }
   
   /**
    * 
    * @see Subject#isAuthenticated()
    */
   public static boolean isAuthenticated()
   {
      return SecurityUtils.getSubject().isAuthenticated();
   }
   
   /**
    * 
    * @see Subject#hasRole(String)
    */
   public static boolean hasRole(String role)
   {
      return SecurityUtils.getSubject().hasRole(role);
   }
   
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
    * Delegate to {@link Subject#hasAllRoles(java.util.Collection)}
    * 
    * @param roles
    * @return
    */
   public static boolean hasAllRoles(String... roles)
   {
      return SecurityUtils.getSubject().hasAllRoles(Arrays.asList(roles));
   }
   
   
   /**
    * 
    *@see Subject#isPermittedAll(String...) 
    */
   public static boolean isPermittedAll(String... permissions)
   {
      return SecurityUtils.getSubject().isPermittedAll(permissions);
   }
   
   /**
    * 
    * @see Subject#isPermitted(String)
    */
   public static boolean isPermitted(String permission)
   {
      return SecurityUtils.getSubject().isPermitted(permission);
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
}
