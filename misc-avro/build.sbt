import net.b83.Dependencies


enablePlugins(JavaAppPackaging)

description := "avro4s examples"

libraryDependencies ++= Seq(
  Dependencies.Compile.avro4s,
  Dependencies.Compile.`avro-tools`
)