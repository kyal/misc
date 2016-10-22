package net.b83

import sbt._
import Keys._


object Dependencies {

  val akkaVersion = "2.4.11"

  object Provided

  object Compile {

    val configs             = "com.github.kxbmap"             %%  "configs"               % "0.5.0-SNAPSHOT" //"0.4.2"
    val `scala-logging`     = "com.typesafe.scala-logging"    %%  "scala-logging"         % "3.5.0"
    val logback             = "ch.qos.logback"                %   "logback-classic"       % "1.1.7"
    val `log4j-over-slf4j`  = "org.slf4j"                     %   "log4j-over-slf4j"      % "1.7.21"
    val avro4s              = "com.sksamuel.avro4s"           %%  "avro4s-core"           % "1.6.1"
    val `avro-tools`        = "org.apache.avro"               %   "avro-tools"            % "1.8.1"
    val `akka-stream-kafka` = "com.typesafe.akka"             %%  "akka-stream-kafka"     % "0.13"

  }

  object Test {
    val specs2              = "org.specs2"                    %%  "specs2-core"           % "3.8.5.1"
    val `akka-testkit`      = "com.typesafe.akka"             %%  "akka-testkit"          % akkaVersion
    val `akka-steam-testkit`= "com.typesafe.akka"             %%  "akka-stream-testkit"   % akkaVersion
    val `akka-slf4j`        = "com.typesafe.akka"             %%  "akka-slf4j"            % akkaVersion
  }


  object Overrides {
    val slf4j              = "org.slf4j"                      %   "slf4j-api"             % "1.7.21"
  }

}