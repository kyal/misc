
description := "Akka examples"


import Dependencies.Compile._
import Dependencies.Test._

libraryDependencies ++= Seq(
  `akka-stream-kafka`,
  `akka-slf4j` % Test,
  `akka-testkit` % Test,
  `akka-steam-testkit` % Test
)
