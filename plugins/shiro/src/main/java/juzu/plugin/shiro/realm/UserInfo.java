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
package juzu.plugin.shiro.realm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class UserInfo implements Serializable
{
   String username;
   
   String password;
   
   Map<String, String> attributes = new HashMap<String, String>();
   
   public UserInfo()
   {
   }
   
   public UserInfo(String username, String password)
   {
      this.username = username;
      this.password = password;
   }
   
   public void setUserName(String username)
   {
      this.username = username;
   }
   
   public String getUserName()
   {
      return username;
   }
   
   public void setPassword(String password)
   {
      this.password = password;
   }
   
   public String getPassword()
   {
      return password;
   }
   
   public Map<String, String> getAttributes()
   {
      return attributes;
   }
   
   public String getAttribute(String key)
   {
      String value =  attributes.get(key);
      if(value == null)
      {
         return attributes.remove(key);
      }
      return value;
   }
   
   public void setAttribute(String key, String value)
   {
      attributes.put(key, value);
   }
}
