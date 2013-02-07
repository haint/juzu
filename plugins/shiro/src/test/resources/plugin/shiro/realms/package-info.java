@juzu.Application
@Shiro(realms = {
   @Realm(value = plugin.shiro.SimpleRealm.class, name = "simple"),
   @Realm(value = plugin.shiro.OtherRealm.class, name = "other")
}) 
package plugin.shiro.realms;
import juzu.plugin.shiro.*;