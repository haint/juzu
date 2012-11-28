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
         //request.getResponse().withHeader("Set-Cookie", "rememberMe=wlmwJ0hIRsw9Fyl8Ap3weZjtuWG4N6EbbMNDpgsrwMWFxEUzXQNRbEduHF1jW6owLakFzfx3Rs9Iu3x+My7gJvJlJf2pFeB6xuNtW3ameAS+u+0Hh9WVycKC3KUFFNB1PSRa2ebSweVqcECR4ydV+Ezy0/8juGPFGDRxF2y8Zeexl3HKZivhnaPCXox9AObO2aIDWmF5BJODoIE+w+WB4/5y3bgUIGDiLwxV5Jsj+l++/fBu3z7g4bDjiaPJy7OO8PB2K0M/93KAEa141+IgBocvVn5mEXpkhKptYRp2Ut8c8t/VY7BvTncboI9Rc+3oMhWKDEiOxSVYshBwBKYE3C6fzqJrldf3eLuh5Gkg5UbIegTmBDBYa6x6UJYoGh1yrDi7mzJKXI1yL3DP7upj9lPDGPIpkkaY9RIgs2jJOYSFzNxxtgwse6XMq247TOR20YAO4cGPc7OaWIceBKTcdbbqfy2B5sDKIKQXaYgMrjWr2dOtr2FpGJUua7FLSukR; Path=/; Max-Age=31536000; Expires=Thu, 28-Nov-2013 10:04:05 GMT; HttpOnly");
      }
   }
}
