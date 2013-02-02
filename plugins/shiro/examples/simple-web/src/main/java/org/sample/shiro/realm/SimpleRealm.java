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

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class SimpleRealm extends AuthorizingRealm
{
   private SimpleUserHandle handle = new SimpleUserHandle();
   
   public SimpleRealm()
   {
      super();
      setCacheManager(new MemoryConstrainedCacheManager());
      setCachingEnabled(true);
   }

   @Override
   protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
   {
      String username = (String)getAvailablePrincipal(principals);
      Set<String> roles = handle.getRoles(username);
      SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
      Set<String> permission = new HashSet<String>();
      for(String role : roles)
      {
         Set<String> perms = handle.getPermissions(username, role);
         if(perms != null)
         {
            permission.addAll(perms);
         }
      }
      info.setStringPermissions(permission);
      return info;
   }

   @Override
   protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
   {
      UserInfo user = handle.findUser((String)token.getPrincipal(), new String((char[])token.getCredentials()));
      return user != null ? new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName()) : null;
   }

   @Override
   public String getName()
   {
      return "simple";
   }
}
