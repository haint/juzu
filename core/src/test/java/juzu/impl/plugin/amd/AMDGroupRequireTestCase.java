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
package juzu.impl.plugin.amd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import juzu.impl.common.Tools;
import juzu.test.UserAgent;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class AMDGroupRequireTestCase extends AbstractAMDTestCase {
  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    WebArchive war = createServletDeployment(true, "plugin.amd.group.require");
    war.addAsWebResource(new StringAsset("define('Foo', function() { return { text : 'Hello' };});"), "js/foo.js");
    war.addAsWebResource(new StringAsset("define('Bar', ['Foo'], function(foo) { return { text : foo.text + ' World' };});"), "js/bar.js");
    return war;
  }

  @Test
  @RunAsClient
  public void test() throws Exception {
    UserAgent ua = assertInitialPage();
    HtmlPage page = ua.getHomePage();
    ua.waitForBackgroundJavaScript(1000);
    
    DomNodeList<DomElement> scripts = page.getElementsByTagName("script");
    
    assertEquals(5, scripts.size());
    
    ArrayList<String> sources = new ArrayList<String>();
    for(DomElement script : scripts) {
      String src = script.getAttribute("src");
      if (src != null && !src.isEmpty()) {
        sources.add(src);
      }
    }
    
    assertList(Tools.list("/juzu/assets/juzu/impl/plugin/amd/require.js",
        "/juzu/assets/juzu/impl/plugin/amd/wrapper.js",
        "/juzu/assets/juzu/impl/plugin/amd/juzu.js"), sources);
    
    List<String> alert = ua.getAlerts(page);
    assertEquals(Collections.singletonList("Hello World"), alert);
  }
}
