@juzu.Application 
@juzu.shiro.Shiro(realms = {@juzu.shiro.Realm(value = plugin.shiro.SimpleRealm.class, name = "simple")}) 
package plugin.shiro.require.controller1;