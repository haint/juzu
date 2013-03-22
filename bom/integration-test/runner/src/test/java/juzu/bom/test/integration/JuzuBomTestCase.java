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
package juzu.bom.test.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.Assert;
import juzu.impl.common.Tools;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
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
public class JuzuBomTestCase {

  @Deployment
  public static WebArchive createDeployment() throws Exception {
    String url = TestCaseController.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
    StringBuilder sb = new StringBuilder();
    sb.append(url.substring(0, url.indexOf(".jar")));
    sb.append("-tomcat-spring.war");
    
    WebArchive war = ShrinkWrap.create(ZipImporter.class, "tomcat-spring.war").importFrom(new File(new URI(sb.toString()))).as(WebArchive.class);
    String servlet = Tools.read(Thread.currentThread().getContextClassLoader().getResource("servlet/web.xml"));
    servlet = String.format(servlet, "spring");
    war.setWebXML(new StringAsset(servlet));

    return war;
  }
  
  @Drone
  WebDriver driver;

  @ArquillianResource
  protected URL deploymentURL;
  
  @Test @RunAsClient
  public void test() throws Exception {
    driver.get(deploymentURL.toURI().toString());
    Assert.assertEquals("pass", driver.findElement(By.tagName("body")).getText());
  }
}
