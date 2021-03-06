/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.bridge.spi.web;

import juzu.Resource;
import juzu.impl.bridge.Bridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.impl.common.UriBuilder;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.plugin.router.RouteDescriptor;
import juzu.impl.plugin.router.RouterDescriptor;
import juzu.impl.plugin.router.RouterPlugin;
import juzu.impl.request.Method;
import juzu.request.RequestParameter;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;
import juzu.impl.router.RouteMatch;
import juzu.impl.router.Router;
import juzu.request.Phase;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Handler implements Closeable {

  /** . */
  private static final Phase[] GET_PHASES = {Phase.VIEW, Phase.ACTION, Phase.RESOURCE};

  /** . */
  private static final Phase[] POST_PHASES = {Phase.ACTION, Phase.VIEW, Phase.RESOURCE};

  /** . */
  private static final Phase[] OTHER_PHASES = {Phase.RESOURCE};

  /** . */
  final Bridge bridge;

  /** . */
  final Route root;

  /** . */
  final HashMap<MethodHandle, Route> forwardRoutes;

  /** . */
  final HashMap<Route, RouteDescriptor> backwardRoutes;

  public Handler(Bridge bridge) throws Exception {
    this.bridge = bridge;

    //
    HashMap<MethodHandle, Route> forwardRoutes = new HashMap<MethodHandle, Route>();
    HashMap<Route, RouteDescriptor> backwardRoutes = new HashMap<Route, RouteDescriptor>();

    //
    Route root = new Router();
    RouterPlugin router = bridge.getApplication().resolveBean(RouterPlugin.class);
    if (router != null) {
      RouterDescriptor desc = router.getDescriptor();
      if (desc != null) {
        Map<RouteDescriptor, Route> ret = desc.popupate(root);
        for (Map.Entry<RouteDescriptor, Route> entry : ret.entrySet()) {
          forwardRoutes.put(entry.getKey().handle, entry.getValue());
          backwardRoutes.put(entry.getValue(), entry.getKey());
        }
      }
    }

    //
    this.forwardRoutes = forwardRoutes;
    this.backwardRoutes = backwardRoutes;
    this.root = root;
  }

  public RouteDescriptor getMethods(Route route) {
    return backwardRoutes.get(route);
  }

  public Route getRoute(MethodHandle method) {
    return forwardRoutes.get(method);
  }

  public Route getRoot() {
    return root;
  }

  public Bridge getBridge() {
    return bridge;
  }

  public void handle(WebBridge bridge) throws Throwable {

    //
    String requestPath = bridge.getRequestContext().getRequestPath();

    // Determine first a possible match from the root route from the request path
    Method requestTarget = null;
    RouteMatch requestMatch = null;
    Map<String, RequestParameter> requestParameters = Collections.emptyMap();
    if (requestPath.startsWith(bridge.getRequestContext().getPath())) {

      //
      juzu.Method requestMethod = bridge.getHttpContext().getMethod();
      Iterator<RouteMatch> matches = root.matcher(requestPath.substring(bridge.getRequestContext().getPath().length()), Collections.<String, String[]>emptyMap());

      // Determine a method
      while (matches.hasNext()) {
        RouteMatch match = matches.next();
        RouteDescriptor routeDesc = getMethods(match.getRoute());
        if (routeDesc != null) {
          Method target = this.bridge.getApplication().resolveBean(ControllerPlugin.class).getDescriptor().getMethodByHandle(routeDesc.handle);
          if (target.getPhase() == Phase.VIEW) {
            if (requestMethod == juzu.Method.POST) {
              requestTarget =  target;
              requestMatch = match;
            } else if (requestMethod == juzu.Method.GET) {
              requestTarget =  target;
              requestMatch = match;
              break;
            }
          } else if (target.getPhase() == Phase.ACTION) {
            if (requestMethod == juzu.Method.GET) {
              requestTarget =  target;
              requestMatch = match;
            } else if (requestMethod == juzu.Method.POST) {
              requestTarget =  target;
              requestMatch = match;
              break;
            }
          } else if (target.getPhase() == Phase.RESOURCE) {
            if (Arrays.asList(target.getMethod().getAnnotation(Resource.class).method()).contains(requestMethod)) {
              requestTarget =  target;
              requestMatch = match;
              break;
            }
          }
        }
      }

      // Determine parameters for the match
      if (requestMatch != null && (requestMatch.getMatched().size() > 0 || bridge.getRequestContext().getParameters().size() > 0)) {
        requestParameters = new HashMap<String, RequestParameter>();
        for (RequestParameter requestParameter : bridge.getRequestContext().getParameters().values()) {
          requestParameters.put(requestParameter.getName(), requestParameter);
        }
        for (Map.Entry<PathParam, String> entry : requestMatch.getMatched().entrySet()) {
          RequestParameter requestParameter = RequestParameter.create(entry.getKey().getName(), entry.getValue());
          requestParameters.put(requestParameter.getName(), requestParameter);
        }
      }
    }

    // No method means we either send a server resource
    // or we look for the handler method
    if (requestTarget == null) {
      // If we have an handler we locate the index method
      requestTarget = this.bridge.getApplication().resolveBean(ControllerPlugin.class).getResolver().resolve(Phase.VIEW, Collections.<String>emptySet());
    }

    // No method -> not found
    if (requestTarget == null) {
      bridge.getRequestContext().setStatus(404);
    } else {
      if (requestMatch == null) {
        Route requestRoute = getRoute(requestTarget.getHandle());
        if (requestRoute != null) {
          requestMatch = requestRoute.matches(Collections.<String, String>emptyMap());
          if (requestMatch != null) {
            StringBuilder sb = new StringBuilder();
            requestMatch.render(new UriBuilder(sb));
            if (!sb.toString().equals(requestPath)) {
              StringBuilder redirect = new StringBuilder();
              bridge.renderRequestURL(redirect);
              redirect.append(sb);
              bridge.getRequestContext().sendRedirect(redirect.toString());
              return;
            }
          }
        }
      }

      //
      WebRequestBridge requestBridge;
      if (requestTarget.getPhase() == Phase.ACTION) {
        requestBridge = new WebActionBridge(this.bridge, this, bridge, requestTarget, requestParameters);
      } else if (requestTarget.getPhase() == Phase.VIEW) {
        requestBridge = new WebViewBridge(this.bridge, this, bridge, requestTarget, requestParameters);
      } else if (requestTarget.getPhase() == Phase.RESOURCE) {
        requestBridge = new WebResourceBridge(this.bridge, this, bridge, requestTarget, requestParameters);
      } else {
        throw new Exception("Cannot decode phase");
      }

      //
      requestBridge.invoke();

      //
      if (requestBridge.send()) {
        // ok
      } else {
        throw new UnsupportedOperationException("Not yet handled by " + requestBridge.getClass().getSimpleName() + ": " + requestBridge.response);
      }
    }
  }

  public void close() throws IOException {
  }
}
