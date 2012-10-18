import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "su-lunch"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "org.jsoup" % "jsoup" % "1.7.1",
      "joda-time" % "joda-time" % "2.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      

      templatesImport ++= Seq("impl.lunchInfo._")
    )

}
