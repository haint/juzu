@juzu.Application 
@Shiro(realms = {@Realm(value = plugin.shiro.SimpleRealm.class, name = "simple")}) 
package plugin.shiro.authc.require;
import juzu.plugin.shiro.*;