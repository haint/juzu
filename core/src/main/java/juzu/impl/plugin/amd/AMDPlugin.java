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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.amd.AMDDependency;
import juzu.impl.asset.amd.AMDMetaData;
import juzu.impl.asset.amd.AMDScriptManager;
import juzu.impl.common.JSON;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.request.Phase;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class AMDPlugin extends ApplicationPlugin implements RequestFilter{

  /** .*/
  private String[] modules;
  
  /** .*/
  private AMDDescriptor descriptor;
  
  /** .*/
  private PluginContext context;

  /** .*/
  @Inject
  @Named("juzu.asset_manager.amd")
  AMDScriptManager manager;
  
  public AMDPlugin() {
    super("amd");
  }
  
  public AssetManager getAMDManager() {
    return manager;
  }
  
  @Override
  public Descriptor init(PluginContext context) throws Exception {
    JSON config = context.getConfig();
    List<AMDMetaData> modules;
    if(config != null) {
      String packageName = config.getString("package");
      AssetLocation location = AssetLocation.safeValueOf(config.getString("location"));
      if (location == null) {
        location = AssetLocation.APPLICATION;
      }
      
      modules = load(packageName, location, config.getList("modules", JSON.class));
    } else {
      modules = Collections.emptyList();
    }
    this.descriptor = new AMDDescriptor(modules);
    this.context = context;
    return descriptor;
  }
  
  private List<AMDMetaData> load(
    String packageName,
    AssetLocation defaultLocation,
    List<? extends JSON> modules) throws Exception {
   List<AMDMetaData> abc = Collections.emptyList();
  if (modules != null && modules.size() > 0) {
    abc = new ArrayList<AMDMetaData>();
    for (JSON module : modules) {
      String name = module.getString("name");
      AssetLocation location = AssetLocation.safeValueOf(module.getString("location"));
      List<JSON> dependencies = (List<JSON>)module.getList("dependencies");
      
      // We handle here location / perhaps we could handle it at compile time instead?
      if (location == null) {
        location = defaultLocation;
      }
      
      if(location == AssetLocation.SERVER && dependencies != null) {
        throw new UnsupportedOperationException("The AMD wrapping supports only for script is located at APPLICATION");
      }

      //
      String value = module.getString("path");
      if (!value.startsWith("/") && location == AssetLocation.APPLICATION) {
        value = "/" + packageName.replace('.', '/') + "/" + value;
      }
      
      //
      String adapter = module.getString("adapter");
      
      //
      AMDMetaData descriptor = new AMDMetaData(
        name,
        location,
        value,
        adapter
      );
      if(dependencies != null && !dependencies.isEmpty()) {
        for(JSON dependency : dependencies) {
          String depName = dependency.getString("name");
          String depAlias = dependency.getString("alias");
          descriptor.addDependency(new AMDDependency(depName, depAlias));
        }
      }

      abc.add(descriptor);
    }
  }
  return abc;
}
  
  @PostConstruct
  public void start() throws Exception {
    URL requirejsURL = AMDPlugin.class.getClassLoader().getResource("juzu/impl/plugin/amd/require.js");
    if (requirejsURL == null) {
      throw new Exception("Not found require.js");
    }
    
    //
    manager.addAMD(
        new AMDMetaData(
            "juzu.amd",
            AssetLocation.APPLICATION,
            "/juzu/impl/plugin/amd/require.js"),
        requirejsURL);
    
    URL wrapperjsURL = AMDPlugin.class.getClassLoader().getResource("juzu/impl/plugin/amd/wrapper.js");
    if (wrapperjsURL == null) {
      throw new Exception("Not found wrapper.js");
    }
    
    //
    manager.addAMD(
      new AMDMetaData(
          "juzu.amd.wrapper",
          AssetLocation.APPLICATION, 
          "/juzu/impl/plugin/amd/wrapper.js"),
      wrapperjsURL);
    
    this.modules = process(descriptor.getModules(), manager);
  }
  
  private String[] process(List<AMDMetaData> modules, AMDScriptManager manager) throws Exception {
    ArrayList<String> assets = new ArrayList<String>();
    for (AMDMetaData module : modules) {

      // Validate assets
      AssetLocation location = module.getLocation();
      URL url;
      if (location == AssetLocation.APPLICATION) {
        url = context.getApplicationResolver().resolve(module.getPath());
        if (url == null) {
          throw new Exception("Could not resolve application  " + module.getPath());
        }
      } else if (location == AssetLocation.SERVER) {
        if (!module.getPath().startsWith("/")) {
          url = context.getServerResolver().resolve("/" + module.getPath());
          if (url == null) {
            throw new Exception("Could not resolve server asset " + module.getPath());
          }
        } else {
          url = null;
        }
      } else {
        url = null;
      }

      //
      String id = manager.addAMD(module, url);
      assets.add(id);
    }

    //
    return assets.toArray(new String[assets.size()]);
  }

  public void invoke(Request request) {
    request.invoke();
    
    //
    if (request.getContext().getPhase() == Phase.VIEW) {
      Response response = request.getResponse();
      if (response instanceof Response.Render && modules.length > 0) {
        Response.Render render = (Response.Render)response;

        //
        PropertyMap properties = new PropertyMap(render.getProperties());
        properties.addValues(PropertyType.AMD, "juzu.amd");
        properties.addValues(PropertyType.AMD, "juzu.amd.wrapper");
        properties.addValues(PropertyType.AMD, modules);

        // Use a new response
        request.setResponse(new Response.Render(properties, render.getStreamable()));
      }
    }
  }
}
