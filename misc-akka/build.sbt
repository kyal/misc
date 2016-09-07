
description := "Akka examples"

libraryDependencies ++= Seq(
  Dependencies.Compile.`akka-stream-kafka`,
  Dependencies.Test.`akka-slf4j` % Test
)
