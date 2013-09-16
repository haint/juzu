@Requires(
  value = {
    @Require(id = "Foo", path="js/foo.js", group = "juzu"),
    @Require(id = "Bar", path = "js/bar.js", group = "juzu")
  },
  location = AssetLocation.SERVER
)
@juzu.Application
package plugin.amd.group.require;
import juzu.plugin.amd.*;
import juzu.asset.AssetLocation;