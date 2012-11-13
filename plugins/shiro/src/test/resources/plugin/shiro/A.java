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
   @RequiresGuest
   public Response index() throws Exception 
   {
      Subject subject = SecurityUtils.getSubject();
      UsernamePasswordToken token = new UsernamePasswordToken("root", "secret");
      subject.login(token);
      if(subject.isAuthenticated())
      {
         System.out.println("The application has been authenticated with " + subject);
      }
      return Render.ok(
         "<a id='guest' href='" + A_.guestURL() +"'>guest</a><br/>" +
         "<a id='user' href='" + A_.userURL() +"'>user</a><br/>" +
         "<a id='authenticate' href='" + A_.authenticateURL() +"'>authenticate</a><br/>" +
         "<a id='role' href='" + A_.roleURL() +"'>role</a><br/>" +
         "<a id='permission' href='" + A_.permissionURL() +"'>permission</a><br/>" +
         "<a id='changeToUser' href='" + A_.changeToUserURL() +"'>changeToUser</a><br/>" +
         "<a id='changeToGuest' href='" + A_.changeToGuestURL() +"'>changeToGuest</a><br/>");
   }
   
   @Resource
   @Route("/change/user")
   public void changeToUser()
   {
      Subject subject = SecurityUtils.getSubject();
      subject.logout();
      UsernamePasswordToken token = new UsernamePasswordToken("haint", "haint");
      subject.login(token);
      if(subject.isAuthenticated())
      {
         System.out.println("The application has been authenticated with " + subject);
      }
   }
   
   @Resource
   @Route("/change/guest")
   public void changeToGuest()
   {
      Subject subject = SecurityUtils.getSubject();
      subject.logout();
   }
   
   @RequiresGuest
   @View
   @Route("/guest")
   public Response guest()
   {
      return Render.ok("pass");
   }
   
   @RequiresUser
   @View
   @Route("/user")
   public Response user()
   {
      return Render.ok("pass");
   }
   
   @RequiresAuthentication
   @View
   @Route("/authenticate")
   public Response authenticate()
   {
      return Render.ok("pass");
   }
   
   @RequiresPermissions(value={"test1", "test2"}, logical=Logical.AND)
   @View
   @Route("/permission")
   public Response permission()
   {
      return Render.ok("pass");
   }
   
   @RequiresRoles(value={"role1", "role2"}, logical=Logical.OR)
   @View
   @Route("/role")
   public Response role()
   {
      return Render.ok("pass");
   }
}
