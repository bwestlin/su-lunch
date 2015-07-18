// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  Classpaths.sbtPluginReleases
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.2")

addSbtPlugin("com.heroku" % "sbt-heroku" % "0.3.4")