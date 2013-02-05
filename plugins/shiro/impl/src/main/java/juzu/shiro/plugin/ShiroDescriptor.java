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
import java.util.Collection;
import java.util.List;

import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.JSON;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.request.Request;
import juzu.shiro.impl.JuzuRememberMe;
import juzu.shiro.impl.SecurityManagerScoped;
import juzu.shiro.impl.ShiroAuthenticator;
import juzu.shiro.impl.ShiroAuthorizor;
import juzu.shiro.impl.SubjectScoped;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

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
  private boolean rememberMeSupported = false;

  /** . */
  private URL shiroIniURL;

  ShiroDescriptor(JSON config) {
    this.rememberMeSupported = config.get("rememberMe") != null ? true : false;
    this.authenticater = new ShiroAuthenticator(rememberMeSupported);
    this.authorizer = new ShiroAuthorizor();
    this.config = config;
  }

  public void setShiroIniURL(URL iniURL) {
    this.shiroIniURL = iniURL;
  }

  public JSON getConfig() {
    return config;
  }

  private void start(Request request) throws InvocationTargetException {
    SecurityManager currentManager =
      getSecurityManager(request.getBridge(), request.getApplication().getInjectionContext());

    //
    Subject currentUser = null;
    if (request.getBridge().getSessionValue("currentUser") != null) {
      currentUser = (Subject)request.getBridge().getSessionValue("currentUser").get();
    } else {
      Subject.Builder builder = new Subject.Builder(currentManager);
      currentUser = builder.buildSubject();
      SubjectScoped subjectValue = new SubjectScoped(currentUser);
      request.getBridge().setSessionValue("currentUser", subjectValue);
    }

    //
    ThreadContext.bind(currentUser);
    ThreadContext.bind(currentManager);
  }

  private SecurityManager getSecurityManager(RequestBridge bridge, InjectionContext context)
    throws InvocationTargetException {

    if (bridge.getSessionValue("currentManager") != null) {
      return (SecurityManager)bridge.getSessionValue("currentManager").get();
    }

    SecurityManager currentManager = null;
    try {
      currentManager = SecurityUtils.getSecurityManager();
    } catch (UnavailableSecurityManagerException e1) {
      if (shiroIniURL != null) {
        Ini ini = new Ini();
        try {
          ini.load(shiroIniURL.openStream());
          IniSecurityManagerFactory factory = new IniSecurityManagerFactory(ini);
          currentManager = factory.getInstance();
        } catch (Exception e2) {
          throw new ApplicationException(e2);
        }
      } else {
        currentManager = new DefaultSecurityManager();
      }
    }

    if (rememberMeSupported && currentManager instanceof DefaultSecurityManager) {
      ((DefaultSecurityManager)currentManager).setRememberMeManager(new JuzuRememberMe());
    }

    if (config.get("realms") != null) {
      injectRealms(currentManager, context);
    }

    SecurityManagerScoped managerValue = new SecurityManagerScoped(currentManager);
    bridge.setSessionValue("currentManager", managerValue);

    return currentManager;
  }

  private void injectRealms(SecurityManager currentManager, InjectionContext manager) throws InvocationTargetException {
    JSON realmsJSON = config.getJSON("realms");

    Iterable beans = manager.resolveBeans(AuthorizingRealm.class);
    for (Object bean : beans) {
      Object instance = manager.create(bean);
      AuthorizingRealm realm = AuthorizingRealm.class.cast(manager.get(bean, instance));
      JSON realmJSON = realmsJSON.getJSON(realm.getClass().getName());
      if (realmJSON != null) {
        if (realmJSON.get("name") != null) {
          realm.setName(realmJSON.getString("name"));
        }

        Collection<Realm> realms = ((RealmSecurityManager)currentManager).getRealms();
        if (realms == null) {
          ((RealmSecurityManager)currentManager).setRealm(realm);
        } else {
          ((RealmSecurityManager)currentManager).getRealms().add(realm);
        }
      }
    }
  }

  private void end() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

  public void invoke(Request request) throws ApplicationException, InvocationTargetException {
    try {
      //
      start(request);

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
    } finally {
      end();
    }
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
