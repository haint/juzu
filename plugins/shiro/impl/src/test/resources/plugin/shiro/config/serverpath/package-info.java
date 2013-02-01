@juzu.Application
@Shiro(config = @Configuration(value = "/WEB-INF/shiro.ini", location = AssetLocation.SERVER))
package plugin.shiro.config.serverpath;

import juzu.shiro.Shiro;
import juzu.shiro.Configuration;
import juzu.asset.AssetLocation;