import com.typesafe.sbt.packager.docker.Cmd

description := "Docker examples"

enablePlugins(JavaAppPackaging, DockerPlugin, ClasspathJarPlugin)

// To add files to the container outside of /opt/docker
dockerCommands += Cmd("ADD", "*", "/")
dockerCmd += "arg1"

//javaOptions in Universal += "-main net.b83.misc.TestAvro4s"

