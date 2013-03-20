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
package juzu.assembly.integration;

import java.net.URL;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
@RunWith(Arquillian.class)
public class AssemblyTomcatITCase extends AbstractITCase {

  @Deployment(testable = false, name = "spring", order = 1)
  public static WebArchive createServletSpring() throws Exception {
    return createServletDeployment("spring");
  }
  
  @Deployment(testable = false, name = "guice", order = 2)
  public static WebArchive createServletGuice() throws Exception {
    return createServletDeployment("guice");
  }
  
  @Deployment(testable = false, name = "weld", order = 3)
  public static WebArchive createServletWeld() throws Exception {
    return createServletDeployment("weld");
  }
  
  @Drone
  WebDriver driver;

  @ArquillianResource
  protected URL deploymentURL;

  @Test @RunAsClient @OperateOnDeployment("spring") 
  public void testSpring() throws Exception {
    driver.get(deploymentURL.toURI().toString());
    Assert.assertEquals("pass", driver.findElement(By.tagName("body")).getText());
  }
  
  @Test @RunAsClient @OperateOnDeployment("guice") 
  public void testGuice() throws Exception {
    driver.get(deploymentURL.toURI().toString());
    Assert.assertEquals("pass", driver.findElement(By.tagName("body")).getText());
  }
  
  @Test @RunAsClient @OperateOnDeployment("weld") 
  public void testWeld() throws Exception {
    driver.get(deploymentURL.toURI().toString());
    Assert.assertEquals("pass", driver.findElement(By.tagName("body")).getText());
  }
}
