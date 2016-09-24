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
    setSystemProperty("config.file", relativeToAppHome("../conf/application.conf"))
  )

  def setSystemProperty(property: String, value: String) = addJavaArg(s"-D$property=$value")

  def addJavaArg(arg: String) = bashScriptExtraDefines += s"""addJava "$arg""""
  def addAppArg(arg: String) = bashScriptExtraDefines += s"""addApp "$arg""""
  def addDebugger(port: Int) = bashScriptExtraDefines += s"""addDebugger "$port""""

  def appHome = "${app_home}"
  def appMainClass = "${app_mainclass}"
  def appClasspath = "${app_classpath}"
  def relativeToAppHome(relativePath: String) = s"""$$(realpath "$appHome/$relativePath")"""
}
