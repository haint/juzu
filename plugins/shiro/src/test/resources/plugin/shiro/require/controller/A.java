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
package plugin.shiro.require.controller;

import javax.inject.Inject;

import juzu.Action;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresUser;

import plugin.shiro.authz.RequireAtControllerTestCase;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
@RequiresUser
public class A
{
   @Inject
   plugin.shiro.SimpleRealm realm;
   
   @View
   @Route("/")
   public Response index(AuthorizationException e) 
   {
      RequireAtControllerTestCase.exception = e;
      return Response.ok("<a id='view' href='" + A_.view() +"'>view</a>" +
      		"<a id='resource' href='" + A_.resource() + "'>resource</a>" +
      		"<a id='action' href='" + A_.action() + "'>action</a>");
   }
   
   @View
   @Route("/view")
   public Response view()
   {
      return Response.ok("pass");
   }
   
   @Resource
   @Route("/resource")
   public Response resource()
   {
      return Response.content(200, "pass");
   }
   
   @Action
   @Route("/action")
   public Response action()
   {
      return A_.view();
   }
}
