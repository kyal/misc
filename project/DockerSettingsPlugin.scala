package net.b83

import sbt._
import Keys._
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.Keys._
import DockerPlugin.autoImport.Docker


object DockerSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = CommonSettingsPlugin && DockerPlugin

  override lazy val projectSettings = Seq(
    dockerRepository := Some("b83"),
    dockerUpdateLatest := true,
    maintainer in Docker := organization.value
  )
}
