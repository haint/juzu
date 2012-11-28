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

import juzu.Response;
import juzu.impl.common.JSON;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.request.Request;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroDescriptor extends Descriptor
{
   /** . */
   private final ShiroAuthorizer authorizer;
   
   /** . */
   private final ShiroAuthenticater authenticater;
   
   ShiroDescriptor(JSON config)
   {
      this.authorizer = new ShiroAuthorizer(config);
      this.authenticater = new ShiroAuthenticater(config);
   }
   
   public void invoke(Request request) throws ApplicationException
   {
      try
      {
         //
         ShiroRequestLifecycle.begin(request);
         
         //
         if(authenticater.invoke(request) != null) 
         {
            authorizer.invoke(request);
         }
      } 
      catch(Exception e)
      {
         e.printStackTrace();
         request.setResponse(Response.content(500, e.getMessage()));
      }
      finally
      {
         ShiroRequestLifecycle.end(request);
      }
   }
}
