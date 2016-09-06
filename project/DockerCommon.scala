package net.b83

import sbt._
import Keys._
import com.typesafe.sbt.packager.docker.{DockerAlias, DockerPlugin}
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager.Docker


object DockerCommon extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = AppCommon && DockerPlugin

  object autoImport {
    lazy val dockerUsername = settingKey[Option[String]]("Username for generating docker tags")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    dockerUsername := Some("b83"),
    //dockerRepository := Some("server:port"),
    dockerAlias := DockerAlias(dockerAlias.value.registryHost, dockerUsername.value, dockerAlias.value.name, dockerAlias.value.tag),
    dockerUpdateLatest := true
  )
}
