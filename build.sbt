name := "su-lunch"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "org.jsoup"   %   "jsoup"           % "1.7.2" withSources,
  "joda-time"   %   "joda-time"       % "2.1" withSources,
  "org.webjars" %%  "webjars-play"    % "2.2.0" withSources,
  "org.webjars" %   "jquery"          % "1.10.2",
  "org.webjars" %   "bootstrap"       % "2.3.2",
  "org.webjars" %   "font-awesome"    % "3.2.1",
  "org.webjars" %   "jquery-transit"  % "0.9.9"
)

play.Project.playScalaSettings

templatesImport ++= Seq(
  "scala.util._"
)
