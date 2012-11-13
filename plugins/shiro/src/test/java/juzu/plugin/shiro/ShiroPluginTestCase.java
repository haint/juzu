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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import juzu.impl.common.Tools;
import juzu.test.protocol.portlet.AbstractPortletTestCase;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */

public class ShiroPluginTestCase extends AbstractPortletTestCase
{
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
     return createDeployment("plugin.shiro");
   }

   @ArquillianResource
   URL deploymentURL;

   @Drone
   WebDriver driver;

   @Test
   @RunAsClient
   public void test() throws Exception {
     URL url = deploymentURL.toURI().resolve("embed/StandalonePortlet").toURL();
     driver.get(url.toString());
     
     WebElement trigger = driver.findElement(By.id("guest"));
     String guestURL = trigger.getAttribute("href");
     
     trigger = driver.findElement(By.id("user"));
     String userURL = trigger.getAttribute("href");
     
     trigger = driver.findElement(By.id("authenticate"));
     String authenticateURL = trigger.getAttribute("href");
     
     trigger = driver.findElement(By.id("role"));
     String roleURL = trigger.getAttribute("href");
     
     trigger = driver.findElement(By.id("permission"));
     String permissionURL = trigger.getAttribute("href");
     
     trigger = driver.findElement(By.id("changeToUser"));
     String changeToUserURL = trigger.getAttribute("href");
     
     trigger = driver.findElement(By.id("changeToGuest"));
     String changeToGuestURL = trigger.getAttribute("href");
     
     //Test with root 
     driver.get(guestURL);
     WebElement body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
     
     driver.get(userURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "pass"));
     
     driver.get(authenticateURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "pass"));
     
     driver.get(roleURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "pass"));
     
     driver.get(permissionURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "pass"));
     
     //Test with user
     driver.get(changeToUserURL);
     
     driver.get(roleURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "pass"));
     
     driver.get(permissionURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
     
     driver.get(guestURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
     
     //Test with guest
     driver.get(changeToGuestURL);
     
     driver.get(userURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "Unauthorization"));
     
     driver.get(guestURL);
     body = driver.findElement(By.tagName("body"));
     assertEquals(1, Tools.count(body.getText(), "pass"));
   }
}
