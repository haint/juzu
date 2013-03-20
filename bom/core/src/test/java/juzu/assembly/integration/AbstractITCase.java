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

import java.io.File;

import juzu.impl.common.Tools;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.servlet.Servlet;

import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.util.UUIDGenerator;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.embed.EmbedServlet;
import org.gatein.pc.portlet.impl.deployment.DeploymentException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.staxnav.ValueType;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class AbstractITCase {
  
  private static String RESOURCE_PATH = "target/test-classes";

  private static WebArchive createBaseDeployment(String injector) throws Exception {
    WebArchive war =
          ShrinkWrap.create(ZipImporter.class, injector + ".war").importFrom(new File("target/assembly-tomcat-" + injector + ".war"))
          .as(WebArchive.class);

    war.addAsLibraries(
      new File(Portlet.class.getProtectionDomain().getCodeSource().getLocation().toURI()),
      new File(Servlet.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
    
    addResources(new File(RESOURCE_PATH + "/juzu"), war);
    return war;
  }
  
  protected static WebArchive createServletDeployment(String injector) throws Exception {
    WebArchive war = createBaseDeployment(injector);
    String servlet = Tools.read(Thread.currentThread().getContextClassLoader().getResource("servlet/web.xml"));
    servlet = String.format(servlet, injector);
    war.setWebXML(new StringAsset(servlet));
    return war;
  }
  
  private static void addResources(File file, WebArchive war) {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        addResources(child, war);
      }
    } else {
      war.addAsWebResource(file, "WEB-INF/classes" + file.getPath().substring(RESOURCE_PATH.length()));
    }
  }
}
