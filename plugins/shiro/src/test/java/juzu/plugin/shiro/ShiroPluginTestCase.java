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
package juzu.plugin.shiro;

import java.net.URL;

import juzu.impl.common.Tools;
import juzu.test.protocol.portlet.AbstractPortletTestCase;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */

public class ShiroPluginTestCase extends AbstractPortletTestCase
{
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
     return createDeployment("plugin.shiro.authz");
   }
   
   /** . */
   private String requireGuestURL = null;
   
   /** . */
   private String requireUserURL = null;
   
   /** . */
   private String requireAuthcURL = null;
   
   /** . */
   private String requireRoleURL = null;
   
   /** . */
   private String requirePermsURL = null;
   
   /** . */
   private String loginWithRootURL = null;
   
   /** . */
   private String loginWithUserURL = null;
   
   /** . */
   private String logoutURL = null;
   
   @Before
   public void init() throws Exception
   {
      URL url = deploymentURL.toURI().resolve("embed/StandalonePortlet").toURL();
      driver.get(url.toString());
      
      WebElement trigger = driver.findElement(By.id("requireGuestURL"));
      requireGuestURL = trigger.getAttribute("href");
      
      trigger = driver.findElement(By.id("requireUserURL"));
      requireUserURL = trigger.getAttribute("href");
      
      trigger = driver.findElement(By.id("requireAuthcURL"));
      requireAuthcURL = trigger.getAttribute("href");
      
      trigger = driver.findElement(By.id("requireRoleURL"));
      requireRoleURL = trigger.getAttribute("href");
      
      trigger = driver.findElement(By.id("requirePermsURL"));
      requirePermsURL = trigger.getAttribute("href");
      
      trigger = driver.findElement(By.id("loginWithRootURL"));
      loginWithRootURL = trigger.getAttribute("href");
      
      trigger = driver.findElement(By.id("loginWithUserURL"));
      loginWithUserURL = trigger.getAttribute("href");
      
      trigger = driver.findElement(By.id("logoutURL"));
      logoutURL = trigger.getAttribute("href");
   }
   
   @AfterClass
   public static void cleanup()
   {
      DefaultSecurityManager sm = (DefaultSecurityManager)SecurityUtils.getSecurityManager();
      SecurityUtils.setSecurityManager(null);
      sm.destroy();
      ThreadContext.remove();
   }

   @ArquillianResource
   URL deploymentURL;

   @Drone
   WebDriver driver;
   
   @Test
   @RunAsClient
   public void testRoot()
   {
      driver.get(loginWithRootURL);
      
      driver.get(requireGuestURL);
      WebElement body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
      
      driver.get(requireUserURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
      
      driver.get(requireAuthcURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
      
      driver.get(requireRoleURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
      
      driver.get(requirePermsURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
   }
   
   @Test
   @RunAsClient
   public void testUser()
   {
      driver.get(loginWithUserURL);
      
      driver.get(requireGuestURL);
      WebElement body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
      
      driver.get(requireUserURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
      
      driver.get(requireAuthcURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
      
      driver.get(requireRoleURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
      
      driver.get(requirePermsURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
   }
   
   @Test
   @RunAsClient
   public void testGuest()
   {
      driver.get(logoutURL);
      
      driver.get(requireGuestURL);
      WebElement body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "pass"));
      
      driver.get(requireUserURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
      
      driver.get(requireAuthcURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
      
      driver.get(requireRoleURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
      
      driver.get(requirePermsURL);
      body = driver.findElement(By.tagName("body"));
      assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
   }
}
