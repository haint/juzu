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

import java.util.Arrays;

import juzu.Scope;
import juzu.impl.common.JSON;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.request.Request;
import juzu.plugin.shiro.impl.SecurityManagerProvider;
import juzu.plugin.shiro.impl.ShiroAuthenticator;
import juzu.plugin.shiro.impl.ShiroAuthorizor;

import org.apache.shiro.mgt.SecurityManager;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ShiroDescriptor extends Descriptor {
  /** . */
  private final ShiroAuthorizor authorizer;

  /** . */
  private final ShiroAuthenticator authenticater;

  /** . */
  private final JSON config;

  /** . */
  private final BeanDescriptor bean;

  ShiroDescriptor(JSON config) {
    this.authenticater = new ShiroAuthenticator(config.get("rememberMe") != null ? true : false);
    this.authorizer = new ShiroAuthorizor();
    this.config = config;
    this.bean =
      BeanDescriptor
        .createFromProvider(SecurityManager.class, Scope.SESSION, null, new SecurityManagerProvider(config));
  }

  public JSON getConfig() {
    return config;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Arrays.asList(bean);
  }

  public void invoke(Request request) {
    //
    String methodId = request.getContext().getMethod().getHandle().toString();
    String controllerId = methodId.substring(0, methodId.indexOf('#'));
    methodId = methodId.substring(controllerId.length() + 1);
    JSON controllerJSON = config.getJSON(controllerId);
    if (controllerJSON == null) {
      request.invoke();
      return;
    }

    //
    JSON methodsJSON = controllerJSON.getJSON("methods");
    JSON methodJSON = null;

    if (controllerJSON.get("require") != null) {
      if (authorizer.isAuthorized(request, controllerJSON)) {
        if (methodsJSON == null) {
          request.invoke();
          return;
        }

        methodJSON = methodsJSON.getJSON(methodId);
        if (methodJSON == null) {
          request.invoke();
          return;
        }

        doInvoke(request, methodJSON);
        return;
      }

      return;
    }

    if (methodsJSON == null) {
      request.invoke();
      return;
    }

    methodJSON = methodsJSON.getJSON(methodId);
    if (methodJSON == null) {
      request.invoke();
      return;
    }

    doInvoke(request, methodJSON);
  }

  private void doInvoke(Request request, JSON json) {
    if (authorizer.isAuthorized(request, json)) {
      if ("login".equals(json.get("operator"))) {
        authenticater.doLogin(request);
      } else if ("logout".equals(json.get("operator"))) {
        authenticater.doLogout(request);
      } else {
        request.invoke();
      }
    }
  }
}
