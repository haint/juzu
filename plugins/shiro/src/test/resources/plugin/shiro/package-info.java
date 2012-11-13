@Application
@Shiro(
   users = {
      @User(username = "root", password = "secret", roles = {"role1", "role2"}),
      @User(username = "haint", password = "haint", roles = {"role2"})
   },
   
   roles = {
      @Role(name = "role1", permissions = {"test1", "test2"}),
      @Role(name = "role2", permissions = {"test2"})
   }
)
package plugin.shiro;

import juzu.Application;
import juzu.plugin.shiro.*;
