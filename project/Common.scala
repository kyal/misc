package net.b83

import sbt._
import Keys._
import Dependencies.Compile._
import Dependencies.Overrides


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
    organizationName := "B83",
    homepage := Some(url("http://b83.net/")),
    startYear := Some(2016),

    scalacOptions ++= DefaultOptions.scalac,
    scalacOptions ++= Seq(
      "-target:jvm-1.8",
      "-encoding", "utf8",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xfatal-warnings",
      "-Xlint",
      "-Xexperimental",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros",
      "-Ybackend:GenBCode",
      "-Ydelambdafy:method",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Xfuture"
    ),

    javacOptions in compile ++= DefaultOptions.javac,
    javacOptions in compile ++= Seq(
      "-source", "1.8",
      "-target", "1.8",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),

    resolvers ++= DefaultOptions.resolvers(snapshot = true),

    libraryDependencies ++= Seq(
      `scala-logging`//,
      //configs // Using dependsOn git source from top-level build.sbt
    ),
    updateOptions := updateOptions.value.withCachedResolution(true),
    conflictManager := ConflictManager.strict,
    dependencyOverrides ++= Set(
      Overrides.slf4j
    ),

    mySetting := file("src").absolutePath,
    myTask := { println(s"Running myTask. mySetting=${myTaskAsSetting.value}"); myTaskAsSetting.value.length },
    myTaskAsSetting := "XXX: " + name.value
  ) ++ testSettings


  lazy val testSettings = Seq(
    fork in Test := true,
    parallelExecution in Test := false,
    scalacOptions in Test += "-Yrangepos",
    libraryDependencies ++= Seq(
      net.b83.Dependencies.Compile.logback % Test,
      net.b83.Dependencies.Test.specs2 % Test
    )
  )


  // For commands / functionality to be only loaded once
  override lazy val globalSettings = Nil
}
