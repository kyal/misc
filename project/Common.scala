package net.b83

import sbt._
import Keys._
import Dependencies.Compile._


object Common extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = plugins.JvmPlugin

  object autoImport {
    lazy val mySetting = settingKey[String]("Example custom setting from plugin")
    lazy val myTask = taskKey[Int]("Example task")
    lazy val myTaskAsSetting = taskKey[String]("A recomputed setting")

    val Dependencies = net.b83.Dependencies
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    organization := "net.b83",
    homepage := Some(url("http://b83.net/")),
    startYear := Some(2016),

    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-Xexperimental",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros",

      "-target:jvm-1.8",
      "-Ybackend:GenBCode",
      "-Ydelambdafy:method"
    ),

    javacOptions in compile ++= Seq(
      "-source", "1.8",
      "-target", "1.8",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),

    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.typesafeRepo("snapshots"),

    libraryDependencies ++= Seq(
      `scala-logging`//,
      //configs // Using dependsOn git source from top-level build.sbt
    ),
    updateOptions := updateOptions.value.withCachedResolution(true),

    mySetting := file("src").absolutePath,
    myTask := { println(s"Running myTask. mySetting=${myTaskAsSetting.value}"); myTaskAsSetting.value.length },
    myTaskAsSetting := "XXX: " + name.value
  )


  // For commands / functionality to be only loaded once
  override lazy val globalSettings = Nil
}
