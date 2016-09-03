package net.b83

import sbt._
import Keys._


object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val mySetting = settingKey[String]("Example custom setting from plugin")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    organization := "net.b83",
    homepage := Some(url("http://b83.net/")),
    startYear := Some(2016),
    libraryDependencies += Dependencies.Compile.`scala-logging`,
    mySetting := "my default"
  )
}
