package net.b83

import sbt._
import Keys._
import com.typesafe.sbt.packager.Keys._
import Dependencies.Compile._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.SbtNativePackager.Universal


/**
 * For projects which generate executables.
 */
object AppCommon extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = Common && JavaAppPackaging

  override lazy val projectSettings = Seq(
    libraryDependencies ++= Seq(
      logback
    ),
    javaOptions in Universal ++= Seq(
      s"-jvm-debug 5005",
      s"-Dapplication.name=${normalizedName.value}",
      s"-J-Xmx1024M"
    ),
    setSystemProperty("config.file", """$(realpath "${app_home}/../conf")/application.conf""")
  )

  // Could make helper methods for interacting with ${app_home} runtime path by using $(realpath "${app_home})
  def setSystemProperty(property: String, value: String) = addJavaArg(s"-D$property=$value")

  def addJavaArg(arg: String) = bashScriptExtraDefines += s"""addJava "$arg""""
}
