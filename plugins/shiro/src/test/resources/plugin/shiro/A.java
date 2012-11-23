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
package plugin.shiro;

import javax.inject.Inject;

import org.apache.catalina.security.SecurityUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;

import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.Response.Render;
import juzu.Route;
import juzu.View;
import juzu.template.Template;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class A
{
   @Route("/")
   @View
   public Response index() throws Exception 
   {
      Subject subject = SecurityUtils.getSubject();
      String info = null;
      if(subject.isAuthenticated())
      {
         info = "<p>Hello <b>" + subject.getPrincipal() + "</b></p>"; 
      }
      else
      {
         info = "<p>Hello <b>Guest</b></p>"; 
      }
      
      
      return Render.ok(
         info +
         "<a id='requireGuestURL' href='" + A_.guestURL() +"'>requireGuestURL</a><br/>" +
         "<a id='requireUserURL' href='" + A_.userURL() +"'>requireUserURL</a><br/>" +
         "<a id='requireAuthcURL' href='" + A_.authenticateURL() +"'>requireAuthcURL</a><br/>" +
         "<a id='requireRoleURL' href='" + A_.roleURL() +"'>requireRoleURL</a><br/>" +
         "<a id='requirePermsURL' href='" + A_.permissionURL() +"'>requirePermsURL</a><br/>" +
         "<a id='loginWithRootURL' href='" + A_.changeToRootURL() +"'>loginWithRootURL</a><br/>" +
         "<a id='loginWithUserURL' href='" + A_.changeToUserURL() +"'>loginWithUserURL</a><br/>" +
         "<a id='logoutURL' href='" + A_.logoutURL() +"'>logoutURL</a><br/>");
   }
   
   @Action
   @Route("/login/root")
   public Response changeToRoot()
   {
      Subject subject = SecurityUtils.getSubject();
      if(subject.isAuthenticated())
      {
         subject.logout();
      }
      UsernamePasswordToken token = new UsernamePasswordToken("root", "secret");
      subject.login(token);
      return A_.index();
   }
   
   @Action
   @Route("/login/user")
   public Response changeToUser()
   {
      Subject subject = SecurityUtils.getSubject();
      if(subject.isAuthenticated())
      {
         subject.logout();
      }
      UsernamePasswordToken token = new UsernamePasswordToken("haint", "haint");
      subject.login(token);
      return A_.index();
   }
   
   @Action
   @Route("/logout")
   public Response logout()
   {
      Subject subject = SecurityUtils.getSubject();
      subject.logout();
      return A_.index();
   }

   @View
   @Route("/guest")
   @RequiresGuest
   public Response guest()
   {
      return Render.ok("pass");
   }

   @View
   @Route("/requireUser")
   @RequiresUser
   public Response user()
   {
      return Render.ok("pass");
   }

   @View
   @Route("/requireAuthc")
   @RequiresAuthentication
   public Response authenticate()
   {
      return Render.ok("pass");
   }

   @View
   @Route("/requirePerm")
   @RequiresPermissions(value={"test1", "test2"}, logical=Logical.AND)
   public Response permission()
   {
      return Render.ok("pass");
   }

   @View
   @Route("/requireRole")
   @RequiresRoles(value={"role1", "role2"}, logical=Logical.OR)
   public Response role()
   {
      return Render.ok("pass");
   }
}
