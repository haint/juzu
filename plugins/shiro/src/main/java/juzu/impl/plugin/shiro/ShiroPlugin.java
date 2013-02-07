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
package juzu.impl.plugin.shiro;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import juzu.asset.AssetLocation;
import juzu.impl.common.JSON;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.resource.ResourceResolver;
import juzu.plugin.shiro.impl.SubjectScoped;

import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ShiroPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  @Inject
  @Named("juzu.resource_resolver.classpath")
  ResourceResolver classPathResolver;

  /** . */
  @Inject
  @Named("juzu.resource_resolver.server")
  ResourceResolver serverResolver;

  /** . */
  @Inject
  SecurityManager manager;

  /** . */
  private ShiroDescriptor descriptor;

  public ShiroPlugin() {
    super("shiro");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    return config != null ? descriptor = new ShiroDescriptor(config) : null;
  }

  @PostConstruct
  public void postConstruct() throws ConfigurationException, IOException {
    URL iniURL = getShiroIniURL();
    if (iniURL != null) {
      Ini ini = new Ini();
      ini.load(iniURL.openStream());
      IniSecurityManagerFactory factory = new IniSecurityManagerFactory(ini);
      manager = factory.getInstance();
    }
  }

  public void invoke(Request request) throws ApplicationException {
    if (descriptor != null) {
      try {
        start();
        descriptor.invoke(request);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } finally {
        ThreadContext.unbindSubject();
        ThreadContext.unbindSecurityManager();
      }
    } else {
      request.invoke();
    }
  }

  private void start() throws InvocationTargetException {
    //
    Request request = Request.getCurrent();
    Subject currentUser = null;

    if (request.getBridge().getSessionValue("currentUser") != null) {
      currentUser = (Subject)request.getBridge().getSessionValue("currentUser").get();
    } else {
      Subject.Builder builder = new Subject.Builder(manager);
      currentUser = builder.buildSubject();
      SubjectScoped subjectValue = new SubjectScoped(currentUser);
      request.getBridge().setSessionValue("currentUser", subjectValue);
    }

    //
    ThreadContext.bind(manager);
    ThreadContext.bind(currentUser);
  }

  private URL getShiroIniURL() throws MalformedURLException {
    JSON json = descriptor.getConfig().getJSON("config");

    if (json == null)
      return null;

    AssetLocation location = AssetLocation.CLASSPATH;
    if (json.get("location") != null) {
      location = AssetLocation.valueOf(json.getString("location"));
    }

    switch (location) {
      case CLASSPATH :
        return classPathResolver.resolve(json.getString("value"));
      case SERVER :
        return serverResolver.resolve(json.getString("value"));
      case URL :
        return new URL(json.getString("value"));
      default :
        return null;
    }
  }
}
