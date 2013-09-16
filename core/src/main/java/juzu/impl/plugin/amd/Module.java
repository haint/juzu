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

package juzu.impl.plugin.amd;

import java.net.URL;

import juzu.PropertyType;
import juzu.asset.AssetLocation;

/**
 * A module exposed by the plugin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Module {

  /** AMD. */
  public static PropertyType<Module> TYPE = new PropertyType<Module>(){};

  /** The module id. */
  final String id;

  /** The module location. */
  final AssetLocation location;

  /** The module URI. */
  final String uri;
  
  /** . */
  final String group;
  
  /** . */
  final URL url;

  public Module(String id, AssetLocation location, String uri, String group, URL url) {
    this.id = id;
    this.location = location;
    this.uri = uri;
    this.group = group;
    this.url = url;
  }

  public String getId() {
    return id;
  }

  public AssetLocation getLocation() {
    return location;
  }

  public String getUri() {
    return uri;
  }
  
  public String getGroup() {
    return group;
  }
  
  public URL getURL() {
    return url;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    else if (obj instanceof Module) {
      Module that = (Module)obj;
      return that.id.equals(id) && that.location.equals(location) && that.uri.equals(uri) && that.url.equals(url);
    }
    return false;
  }
}
