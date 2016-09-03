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
