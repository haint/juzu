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
package juzu.plugin.shiro.mgt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

import juzu.Response;
import juzu.impl.request.Request;
import juzu.request.HttpContext;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.AbstractRememberMeManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class JuzuRememberMe extends AbstractRememberMeManager
{
   
   /**
    * The default name of the underlying rememberMe cookie which is {@code rememberMe}.
    */
   private final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "rememberMe";
   
   /**
    * The value of deleted cookie (with the maxAge 0).
    */
   private final String DELETED_COOKIE_VALUE = "deleteMe";

   /**
    * The number of seconds in one year (= 60 * 60 * 24 * 365).
    */
   private final int ONE_YEAR = 60 * 60 * 24 * 365;
   
   /** . */
   private long DAY_MILLIS = 86400000; //1 day = 86,400,000 milliseconds
   
   /** . */
   private final static String GMT_TIME_ZONE_ID = "GMT";
   
   /** . */
   private final static String COOKIE_DATE_FORMAT_STRING = "EEE, dd-MMM-yyyy HH:mm:ss z";
   
   /** . */
   private final String NAME_VALUE_DELIMITER = "=";
   
   /** . */
   private final String ATTRIBUTE_DELIMITER = "; ";
   
   /** . */
   private final String COOKIE_HEADER_NAME = "Set-Cookie";
   
   /** . */
   private final String PATH_ATTRIBUTE_NAME = "Path";
   
   /** . */
   private final String EXPIRES_ATTRIBUTE_NAME = "Expires";
   
   /** . */
   private final String MAXAGE_ATTRIBUTE_NAME = "Max-Age";
   
   /** .*/
   private final String DOMAIN_ATTRIBUTE_NAME = "Domain";
   
   @Override
   public void onLogout(Subject subject) 
   {
      Request.getCurrent().invoke();
      super.onLogout(subject);
   }
   
   @Override
   public void onSuccessfulLogin(Subject subject, AuthenticationToken token, AuthenticationInfo info)
   {
      Request.getCurrent().invoke();
      super.onSuccessfulLogin(subject, token, info);
   }
   
   public void forgetIdentity(SubjectContext subjectContext)
   {
      forgetIdentity();
   }
   
   @Override
   protected void forgetIdentity(Subject subject)
   {
      forgetIdentity();
   }
   
   private void forgetIdentity()
   {
      Response response = Request.getCurrent().getResponse();
      HttpContext context = Request.getCurrent().getContext().getHttpContext();
      
      String name = DEFAULT_REMEMBER_ME_COOKIE_NAME;
      String value = DELETED_COOKIE_VALUE;
      String domain = context.getServerName();
      String path = context.getContextPath();
      int maxAge = 0; //always zero for deletion
      String headerValue = buildHeaderValue(name, value, domain.trim(), path.trim(), maxAge);
      response.withHeader(COOKIE_HEADER_NAME, headerValue);
   }
   
   private String buildHeaderValue(String name, String value, String domain, String path, int maxAge)
   {
      StringBuilder sb = new StringBuilder(name).append(NAME_VALUE_DELIMITER);
      if(value != null && !value.isEmpty())
      {
         sb.append(value);
      }
      if(domain != null && !domain.isEmpty())
      {
         sb.append(ATTRIBUTE_DELIMITER);
         sb.append(DOMAIN_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(domain);
      }
      if(path != null && !path.isEmpty())
      {
         sb.append(ATTRIBUTE_DELIMITER);
         sb.append(PATH_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(path);
      }
      
      if (maxAge >= 0) 
      {
         sb.append(ATTRIBUTE_DELIMITER);
         sb.append(MAXAGE_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(maxAge);
         sb.append(ATTRIBUTE_DELIMITER);
         Date expires;
         if (maxAge == 0) {
             //delete the cookie by specifying a time in the past (1 day ago):
             expires = new Date(System.currentTimeMillis() - DAY_MILLIS);
         } else {
             //Value is in seconds.  So take 'now' and add that many seconds, and that's our expiration date:
             Calendar cal = Calendar.getInstance();
             cal.add(Calendar.SECOND, maxAge);
             expires = cal.getTime();
         }
         String formatted = toCookieDate(expires);
         sb.append(EXPIRES_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(formatted);
     }
      return sb.toString();
   }
   
   /**
    * Formats a date into a cookie date compatible string (Netscape's specification).
    *
    * @param date the date to format
    * @return an HTTP 1.0/1.1 Cookie compatible date string (GMT-based).
    */
   private static String toCookieDate(Date date) {
       TimeZone tz = TimeZone.getTimeZone(GMT_TIME_ZONE_ID);
       DateFormat fmt = new SimpleDateFormat(COOKIE_DATE_FORMAT_STRING, Locale.US);
       fmt.setTimeZone(tz);
       return fmt.format(date);
   }
   
   @Override
   protected void rememberSerializedIdentity(Subject subject, byte[] serialized)
   {
      //base 64 encode it and store as a cookie:
      String base64 = Base64.encodeToString(serialized);
      Response response = Request.getCurrent().getResponse();
      HttpContext context = Request.getCurrent().getContext().getHttpContext();
      
      String name = DEFAULT_REMEMBER_ME_COOKIE_NAME;
      String value = base64;
      String domain = context.getServerName();
      String path = context.getContextPath();
      int maxAge = ONE_YEAR; //always zero for deletion
      String headerValue = buildHeaderValue(name, value, domain.trim(), path.trim(), maxAge);
      response.withHeader(COOKIE_HEADER_NAME, headerValue);
   }

   @Override
   protected byte[] getRememberedSerializedIdentity(SubjectContext subjectContext)
   {
      String base64 = readCookieValue(DEFAULT_REMEMBER_ME_COOKIE_NAME);
      if (DELETED_COOKIE_VALUE.equals(base64)) 
      {
         return null;
      }
      
      if(base64 != null)
      {
         base64 = ensurePadding(base64);
         return Base64.decode(base64);
      }
      return null;
   }
   
   /**
    * Sometimes a user agent will send the rememberMe cookie value without padding,
    * most likely because {@code =} is a separator in the cookie header.
    * <p/>
    * Contributed by Luis Arias.  Thanks Luis!
    *
    * @param base64 the base64 encoded String that may need to be padded
    * @return the base64 String padded if necessary.
    */
   private String ensurePadding(String base64) {
       int length = base64.length();
       if (length % 4 != 0) {
           StringBuilder sb = new StringBuilder(base64);
           for (int i = 0; i < length % 4; ++i) {
               sb.append('=');
           }
           base64 = sb.toString();
       }
       return base64;
   }
   
   private String readCookieValue(String name)
   {
      HttpContext context = Request.getCurrent().getBridge().getHttpContext();
      Cookie[] cookies = context.getCookies();
      if(cookies != null)
      {
         for(Cookie cookie : cookies)
         {
            if(cookie.getName().equals(name))
            {
               return cookie.getValue();
            }
         }
      }
      return null;
   }
}
