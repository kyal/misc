
lazy val `misc-akka` = project
lazy val `misc-ammonite` = project
lazy val `misc-avro` = project

lazy val `misc-docker` = project
  .dependsOn(
    `misc-avro`,
    ProjectRef(uri("git://github.com/kxbmap/configs"), "core")
)

scalaVersion in ThisBuild := "2.11.8"
logLevel in (ThisBuild, avro2Class) := Level.Warn


enablePlugins(BuildInfoPlugin)
buildInfoPackage := organization.value
buildInfoUsePackageAsPath := true