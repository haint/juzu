@juzu.plugin.amd.AMD(
  modules = {
    @juzu.plugin.amd.Define(name = "Foo", path="foo.js"),
    @juzu.plugin.amd.Define(
      name = "Bar",
      path="bar.js",
      dependencies = {@juzu.plugin.amd.Dependency(name = "Foo", alias = "foo")}
    )
  }
)
@juzu.Application
package plugin.amd.modular;