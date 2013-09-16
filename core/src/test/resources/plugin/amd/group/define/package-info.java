@juzu.plugin.amd.Defines({
    @juzu.plugin.amd.Define(id = "Foo", path="foo.js", group = "juzu"),
    @juzu.plugin.amd.Define(
      id = "Bar",
      path = "bar.js",
      group = "juzu",
      dependencies = {@juzu.plugin.amd.Dependency(id = "Foo", alias = "foo")}
    )
})
@juzu.Application
package plugin.amd.group.define;