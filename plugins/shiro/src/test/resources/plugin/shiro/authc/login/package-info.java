@juzu.Application
@Shiro(realms = {@Realm(value = plugin.shiro.SimpleRealm.class, name = "simple")}) 
package plugin.shiro.authc.login;
import juzu.plugin.shiro.*;
