import PlayKeys._
import org.joda.time.DateTime, org.joda.time.format.DateTimeFormat
import scala.util.Try
import java.net.InetAddress

name := "su-lunch"

herokuAppName in Compile := "su-lunch"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  ws,
  cache,
  "org.jsoup"   %   "jsoup"           % "1.8.1" withSources,
  "joda-time"   %   "joda-time"       % "2.7" withSources,
  "org.webjars" %%  "webjars-play"    % "2.3.0" withSources,
  "org.webjars" %   "jquery"          % "1.11.2",
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
  "gitCommit" -> gitCommit,
  "buildTime" -> buildTime,
  "buildHost" -> buildHost
)

def getEnv(name: String) = Option(System.getenv(name)).filterNot(_.isEmpty)

def gitBranch = getEnv("TRAVIS_BRANCH") orElse Try("git rev-parse --abbrev-ref HEAD".!!.trim).toOption getOrElse "?"

def gitCommit = getEnv("TRAVIS_COMMIT") orElse Try("git rev-parse HEAD".!!.trim).toOption getOrElse "?"

def buildTime = DateTimeFormat.forPattern("E, yyyy-MM-dd HH:mm:ss Z").print(new DateTime())

def buildHost = Try("hostname --fqdn".!!.trim) orElse Try(InetAddress.getLocalHost.getHostName) getOrElse "?"