@juzu.plugin.amd.AMD(
  modules = {
    @juzu.plugin.amd.Module(name = "Foo", path="foo.js"),
    @juzu.plugin.amd.Module(name = "Bar", path="bar.js")
  }
)
@juzu.Application
package plugin.amd;