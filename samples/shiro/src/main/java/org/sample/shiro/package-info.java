@Application(defaultController=org.sample.shiro.Controller.class)
@Portlet(name="JuzuShiroPortlet")

@Assets(
   stylesheets = {
      @Stylesheet(src="css/style.css", location=AssetLocation.SERVER)
   }
)
@Shiro(rememberMe=true)
package org.sample.shiro;

import juzu.Application;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.shiro.Shiro;
