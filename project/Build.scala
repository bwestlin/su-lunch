import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "su-lunch"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "org.jsoup" % "jsoup" % "1.7.1",
      "joda-time" % "joda-time" % "2.1",
      "org.webjars" %% "webjars-play" % "2.1.0-2",
      "org.webjars" % "jquery" % "1.9.1",
      "org.webjars" % "bootstrap" % "2.1.1",
      "org.webjars" % "font-awesome" % "3.0.2"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      

      //templatesImport ++= Seq("impl.lunchInfo._")
      templatesImport ++= Seq(
        "scala.util._"
      )
    )

}
