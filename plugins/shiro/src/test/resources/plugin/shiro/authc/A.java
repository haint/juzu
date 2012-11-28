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

package plugin.shiro.authc;

import javax.inject.Inject;

import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Response.Render;
import juzu.Route;
import juzu.View;
import juzu.plugin.shiro.Login;
import juzu.plugin.shiro.LoginFailed;
import juzu.plugin.shiro.Logout;
import juzu.template.Template;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class A
{
   
   @Inject
   SimpleUserHandle realmHandle;
   
   @Inject
   @Path("index.gtmpl")
   Template index;

   @View @Route("/")
   public Response index()
   {
      return index.render();
   }
   
   @Action @Route("/login") @Login(usernameParamName="uname", passwordParamName="passwd")
   public Response login(String uname, String passwd)
   {
      return A_.afterLogin(uname);
   }
   
   @View @Route("/afterLogin")
   public Response afterLogin(String username)
   {
      return Render.ok("logged with " + username);
   }
   
   @View @Route("/login/failed") @LoginFailed
   public Response loginFailed()
   {
      return Render.ok("Incorrect username or password");
   }
   
   @Action @Route("/logout") @Logout
   public Response logout()
   {
      return A_.afterLogout();
   }
   
   @View @Route("/afterLogout")
   public Response afterLogout()
   {
      return Render.ok("goodbye");
   }
   
   @View @Route("/user") @RequiresUser
   public Response user()
   {
      return Render.ok("pass");
   }
   
   @View @Route("/guest") @RequiresGuest
   public Response guest()
   {
      return Render.ok("pass");
   }
}
