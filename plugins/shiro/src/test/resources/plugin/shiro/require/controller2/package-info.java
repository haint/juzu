@juzu.Application
@Shiro(realms = {@Realm(value = plugin.shiro.SimpleRealm.class, name = "simple")}) 
package plugin.shiro.require.controller2;
import juzu.plugin.shiro.*;