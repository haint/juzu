@juzu.Application 
@Shiro(realms = {@Realm(value = plugin.shiro.SimpleRealm.class, name = "simple")}) 
package plugin.shiro.authz;
import juzu.plugin.shiro.*;