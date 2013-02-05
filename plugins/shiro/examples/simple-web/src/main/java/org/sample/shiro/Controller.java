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
import juzu.shiro.Login;
import juzu.shiro.Logout;
import juzu.shiro.impl.common.ShiroTools;
import juzu.template.Template;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class Controller {
  private final String[] ROLES = {"admin", "president", "darklord", "goodguy", "schwartz"};

  @Inject
  @Path("index.gtmpl")
  org.sample.shiro.templates.index index;

  @View
  @Route("/")
  public void index() {
    String currentUser = (String)SecurityUtils.getSubject().getPrincipal();
    LinkedList<String> hasRoles = new LinkedList<String>();
    LinkedList<String> lackRoles = new LinkedList<String>();
    for (String role : ROLES) {
      if (ShiroTools.hasRole(role)) {
        hasRoles.add(role);
      } else {
        lackRoles.add(role);
      }
    }

    index.with().user(currentUser == null ? "guest" : currentUser).hasRoles(hasRoles.toArray())
      .lackRoles(lackRoles.toArray()).render();
  }

  @Inject
  @Path("loginform.gtmpl")
  Template loginForm;

  @View
  @Route("/loginForm")
  @RequiresGuest
  public Response loginForm(AuthorizationException e) {
    return e == null ? loginForm.render() : Response.ok("you must <a href='" + Controller_.logout()
      + "'>logout</a> before login again");
  }

  @Action
  @Route("/doLogin")
  @Login(username = "uname", password = "pwd", rememberMe = "remember")
  public Response doLogin(AuthenticationException ex) {
    return ex == null ? Controller_.index() : Controller_.loginForm();
  }

  @Action
  @Route("/logout")
  @Logout
  @RequiresUser
  public Response logout(AuthorizationException e) {
    return e == null ? Controller_.index() : Controller_.loginForm();
  }

  @Inject
  @Path("account.gtmpl")
  Template account;

  @View
  @Route("/account")
  @RequiresUser
  public Response account(AuthorizationException ex) {
    return ex == null ? account.render() : loginForm.render();
  }

  @View
  @Route("/admin")
  @RequiresRoles("admin")
  public Response admin(AuthorizationException ex) {
    return ex == null ? Response.ok("ADMIN AREA") : Response.ok("You dont have admin role");
  }
}
