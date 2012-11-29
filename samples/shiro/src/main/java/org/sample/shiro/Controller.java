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
package org.sample.shiro;

import java.util.LinkedList;

import javax.inject.Inject;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.shiro.Login;
import juzu.plugin.shiro.LoginFailed;
import juzu.plugin.shiro.LoginForm;
import juzu.plugin.shiro.Logout;
import juzu.plugin.shiro.common.ShiroTools;
import juzu.template.Template;

import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.sample.shiro.realm.SimpleUserHandle;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class Controller
{
   private final String[] ROLES = { "admin", "president", "darklord", "goodguy", "schwartz" };
   
   @Inject
   SimpleUserHandle userHandle;
   
   @Inject @Path("index.gtmpl") org.sample.shiro.templates.index index;
   
   @View @Route("/")
   public void index()
   {
      String currentUser = ShiroTools.getCurrentUser();
      LinkedList<String> hasRoles = new LinkedList<String>();
      LinkedList<String> lackRoles = new LinkedList<String>();
      for(String role : ROLES)
      {
         if(ShiroTools.hasRole(role))
         {
            hasRoles.add(role);
         }
         else
         {
            lackRoles.add(role);
         }
      }
      
      index.with()
         .user(currentUser == null ? "guest" : currentUser)
         .hasRoles(hasRoles.toArray())
         .lackRoles(lackRoles.toArray())
         .render();
   }
   
   @Inject @Path("loginform.gtmpl") Template loginForm;
   
   @View @Route("/loginForm") @RequiresGuest @LoginForm
   public void loginForm()
   {
      loginForm.render();
   }
   
   @Action @Route("/doLogin") @Login
   public Response doLogin(String username, String password, String rememberMe)
   {
      return Controller_.index();
   }
   
   @Action @Route("/login/failed") @LoginFailed
   public Response loginFailed()
   {
      return Controller_.loginForm();
   }
   
   @Action @Route("/logout") @Logout
   public Response logout()
   {
      return Controller_.index();
   }
   
   @Inject
   @Path("account.gtmpl")
   Template account;
   
   @View @Route("/account") @RequiresUser
   public void account()
   {
      account.render();
   }
}
