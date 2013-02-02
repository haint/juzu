@juzu.Application
@juzu.shiro.Shiro(realms = {
   @juzu.shiro.Realm(value = plugin.shiro.SimpleRealm.class, name = "simple"),
   @juzu.shiro.Realm(value = plugin.shiro.OtherRealm.class, name = "other")
}) 
package plugin.shiro.realms;