import sbt._
import Keys._


object Dependencies {

  object Compile {

    val logback         = "ch.qos.logback"                %  "logback-classic"      % "1.1.7"
    val `scala-logging` = "com.typesafe.scala-logging"    %% "scala-logging"        % "3.4.0"
    val configs         = "com.github.kxbmap"             %% "configs"              % "0.4.2"


    object Test {
      val specs2        = "org.specs2"                    %% "specs2-core"          % "3.8.4"   % "test"
    }


    object Provided
  }



}