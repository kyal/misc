enablePlugins(BuildInfoPlugin)

scalaVersion in Global := "2.11.8"

buildInfoPackage := organization.value
buildInfoUsePackageAsPath := true

lazy val `misc-akka` = project
lazy val `misc-ammonite` = project
lazy val `misc-docker` = project
lazy val `misc-avro` = project


