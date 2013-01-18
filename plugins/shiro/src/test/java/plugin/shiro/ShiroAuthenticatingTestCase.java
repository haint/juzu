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

import java.net.URL;

import juzu.test.AbstractWebTestCase;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class ShiroAuthenticatingTestCase extends AbstractWebTestCase
{
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive war = createServletDeployment(true, "plugin.shiro.authc");
      war.addPackages(true, SimpleRealm.class.getPackage());
      return war; 
   }
   
   /** . */
   private WebDriver driver;
   
   @Override
   public void setUp()
   {
      driver = new HtmlUnitDriver();
   }
   
   @Override
   public void tearDown()
   {
      driver.quit();
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

   @Test
   @RunAsClient
   public void testLoginSuccess() throws Exception 
   {
      URL url = deploymentURL.toURI().resolve("authc").toURL();
      driver.get(url.toString());
      
      String guestLink = driver.findElement(By.id("guest")).getAttribute("href");
      String userLink = driver.findElement(By.id("user")).getAttribute("href");
      String logoutLink = driver.findElement(By.id("logout")).getAttribute("href");
      
      WebElement username = driver.findElement(By.id("uname"));
      username.sendKeys("root");
      
      WebElement password = driver.findElement(By.id("passwd"));
      password.sendKeys("secret");
      
      WebElement submit = driver.findElement(By.id("submit"));
      submit.click();
      
      waitForPresent("logged with root");
      
      driver.get(guestLink);
      waitForPresent("Unauthorized");
      
      driver.get(userLink);
      waitForPresent("pass");
      
      driver.get(logoutLink);
      waitForPresent("goodbye");
   }
   
   @Test
   @RunAsClient
   public void testLoginFailed() throws Exception
   {
      URL url = deploymentURL.toURI().resolve("authc").toURL();
      driver.get(url.toString());
      
      String guestLink = driver.findElement(By.id("guest")).getAttribute("href");
      String userLink = driver.findElement(By.id("user")).getAttribute("href");
      
      WebElement username = driver.findElement(By.id("uname"));
      username.sendKeys("foo");
      
      WebElement password = driver.findElement(By.id("passwd"));
      password.sendKeys("foo");
      
      WebElement submit = driver.findElement(By.id("submit"));
      submit.click();
      
      waitForPresent("Incorrect username or password");
      
      driver.get(guestLink);
      waitForPresent("pass");
      
      driver.get(userLink);
      waitForPresent("Unauthorized");
   }
   
   
   private void waitForPresent(String text) throws InterruptedException
   {
      for (int second = 0;; second++) {
         if (second >= 60) fail("timeout");
         try 
         {
            if (driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*"+ text +"[\\s\\S]*$"))
            {
               break;
            }
         } 
         catch (Exception e) 
         {
         }
         Thread.sleep(1000);
      }
   }
}
