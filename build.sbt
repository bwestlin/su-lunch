import PlayKeys._
import org.joda.time.DateTime, org.joda.time.format.DateTimeFormat
import scala.util.Try

name := "su-lunch"

herokuAppName in Compile := "su-lunch"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  ws,
  cache,
  "org.jsoup"   %   "jsoup"           % "1.7.2" withSources,
  "joda-time"   %   "joda-time"       % "2.3" withSources,
  "org.webjars" %%  "webjars-play"    % "2.3.0" withSources,
  "org.webjars" %   "jquery"          % "1.10.2",
  "org.webjars" %   "bootstrap"       % "2.3.2",
  "org.webjars" %   "font-awesome"    % "3.2.1",
  "org.webjars" %   "jquery-transit"  % "0.9.9"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

TwirlKeys.templateImports ++= Seq(
  "scala.util._"
)

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>;Global;Routes;controllers.Reverse*;controllers.javascript.*;controllers.ref.*"

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  scalaVersion,
  sbtVersion,
  "gitBranch" -> gitBranch,
  "gitRevision" -> gitRevision,
  "buildTime" -> buildTime
)

def gitBranch = Try("git rev-parse --abbrev-ref HEAD".!!.trim).getOrElse("?")

def gitRevision = Try("git rev-parse HEAD".!!.trim).getOrElse("?")

def buildTime = DateTimeFormat.forPattern("E, yyyy-MM-dd HH:mm:ss Z").print(new DateTime())