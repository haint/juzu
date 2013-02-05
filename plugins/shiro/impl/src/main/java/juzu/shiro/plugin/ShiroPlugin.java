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
package juzu.shiro.plugin;

import java.lang.reflect.InvocationTargetException;
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

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ShiroPlugin extends ApplicationPlugin implements RequestFilter {
  /** . */
  private ShiroDescriptor descriptor;

  /** . */
  @Inject
  @Named("juzu.resource_resolver.classpath")
  ResourceResolver classPathResolver;

  /** . */
  @Inject
  @Named("juzu.resource_resolver.server")
  ResourceResolver serverResolver;

  @PostConstruct
  public void start() throws Exception {
    JSON config = descriptor.getConfig().getJSON("config");
    if (config == null)
      return;

    AssetLocation location = AssetLocation.CLASSPATH;
    if (config.get("location") != null) {
      location = AssetLocation.valueOf(config.getString("location"));
    }

    URL iniURL = null;
    switch (location) {
      case CLASSPATH :
        iniURL = classPathResolver.resolve(config.getString("value"));
        break;
      case SERVER :
        iniURL = serverResolver.resolve(config.getString("value"));
        break;
      case URL :
        iniURL = new URL(config.getString("value"));
        break;
      default :
        break;
    }

    descriptor.setShiroIniURL(iniURL);
  }

  public ShiroPlugin() {
    super("shiro");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    if (config != null) {
      return descriptor = new ShiroDescriptor(config);
    }
    return null;
  }

  public void invoke(Request request) throws ApplicationException {
    if (descriptor != null) {
      try {
        descriptor.invoke(request);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    } else {
      request.invoke();
    }
  }
}
