# Misc Notes


## SBT
`projectA.aggregate(projectB, projectC)` will run tasks on all aggregated projects
`projectA.dependsOn(projectB, projectC)` for classpath dependencies (also will run compile tasks in order)

### Plugins 


#### sbt-release
https://github.com/sbt/sbt-release
```
release
```
1. Checks there are no outstanding changes
2. Asks to confirm current release version and next development version
3. Runs `test:test`
4. Writes release version to `version.sbt`
5. Commit changes to `version.sbt` and tags commit
6. `publish`
7. Updates `version in ThisBuild := "nextVersion"` in `version.sbt`
8. Commits `version.sbt` changes


#### sbt-updates
https://github.com/rtimush/sbt-updates
Running `dependencyUpdates` will show which dependencies can be updated from Maven for each project.


#### sbt-buildinfo
https://github.com/sbt/sbt-buildinfo
Add a `BuildInfo` object to `src-managed` containing project name, version and anything custom put onto `buildInfoKeys`.
Change namespace of `BuildInfo` object using `buildInfoPackage := "name"`
Change object name with `buildInfoObject := "MyBuildInfo"`
```
buildInfoKeys ++= Seq[BuildInfoKey](
  "xxx" -> 68,
  BuildInfoKey.action("buildTime") {
    System.currentTimeMillis
  },
  libraryDependencies
)   
```

## Avro
Why don't unions work? Avro should be looking up a class by name for each of them. Just force the return type
to be Any and force the generated field type to be `T <: GenericRecord`. We can then do pattern matching.

With scala annotation macros you could stub out a class / trait hierarchy and have the Avro fields filled in?
```
trait Base

@FromSchema("Msg1.avsc")
class Msg1 extends Base
```
But you still need the stub classes. Unless you macro annotate a package or parent object or something.