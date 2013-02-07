@juzu.Application 
@Shiro(realms = {@Realm(value = plugin.shiro.SimpleRealm.class, name = "simple")}) 
package plugin.shiro.require.controller1;
import juzu.plugin.shiro.*;