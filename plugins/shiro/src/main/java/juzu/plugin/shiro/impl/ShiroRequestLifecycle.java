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

import java.lang.reflect.InvocationTargetException;

import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.request.Request;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroRequestLifecycle
{

   public static void begin(Request request) throws InvocationTargetException
   {
      DefaultSecurityManager sm = (DefaultSecurityManager)SecurityUtils.getSecurityManager();
      BeanLifeCycle bean = request.getApplication().getInjectionContext().get(AuthorizingRealm.class);
      if(bean != null)
      {
               sm.setRealm((Realm)bean.get());
      }

      Subject currentUser = null;
      if(request.getBridge().getSessionValue("currentUser") != null)
      {
         currentUser = (Subject)request.getBridge().getSessionValue("currentUser").get();
      }
      else
      {
         Subject.Builder builder = new Subject.Builder();
         currentUser = builder.buildSubject();
         ShiroScoped value = new ShiroScoped(currentUser);
         request.getBridge().setSessionValue("currentUser", value);
      }

      //
      ThreadContext.bind(currentUser);
   }

   public static void end(Request request)
   {
      ThreadContext.unbindSubject();
   }
}
