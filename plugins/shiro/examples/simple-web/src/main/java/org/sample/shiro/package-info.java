@Application(defaultController = org.sample.shiro.Controller.class)
@Portlet(name = "JuzuShiroPortlet")
@Assets(stylesheets = {@Stylesheet(src = "css/style.css", location = AssetLocation.SERVER)})
@juzu.plugin.servlet.Servlet(value = "/")
@Shiro(rememberMe = true, realms = {@Realm(value = org.sample.shiro.realm.SimpleRealm.class, name = "simple")})
@Bindings(value = {@Binding(value = org.sample.shiro.realm.SimpleRealm.class)})
package org.sample.shiro;

import juzu.Application;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.portlet.Portlet;
import juzu.shiro.Shiro;
import juzu.shiro.Realm;
import juzu.plugin.binding.Bindings;
import juzu.plugin.binding.Binding;

