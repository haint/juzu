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
package juzu.plugin.shiro;

import java.util.HashMap;
import java.util.Map;

import juzu.impl.common.Tools;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class SimpleRealm extends AuthorizingRealm
{
   
   /** .  */
   private final Map<String, AuthenticationInfo> users = new HashMap<String, AuthenticationInfo>();
   
   /** . */
   private final Map<String, AuthorizationInfo> roles = new HashMap<String, AuthorizationInfo>();
   
   @Override
   protected void onInit()
   {
      //
      users.put("root", new SimpleAuthenticationInfo("root", "secret".toCharArray(), getName()));
      users.put("haint", new SimpleAuthenticationInfo("haint", "haint".toCharArray(), getName()));
      
      //
      SimpleAuthorizationInfo rootRoles = new SimpleAuthorizationInfo(Tools.set("role1", "role2"));
      rootRoles.setStringPermissions(Tools.set("test1", "test2"));
      roles.put("root", rootRoles);
      SimpleAuthorizationInfo userRoles = new SimpleAuthorizationInfo(Tools.set("role2"));
      userRoles.setStringPermissions(Tools.set("test2"));
      roles.put("haint", userRoles);
   }
   
   @Override
   protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
   {
      String principal = (String)getAvailablePrincipal(principals);
      return roles.get(principal);
   }

   @Override
   protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
   {
      return users.get(token.getPrincipal());
   }
}
