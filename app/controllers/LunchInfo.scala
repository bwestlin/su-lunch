package controllers

import play.api._
import cache.Cached
import play.api.Play.current
import libs.concurrent.Akka
import mvc._
import play.api.libs.json._

import impl.lunchInfo._

object LunchInfo extends Controller {

  def index = Action { request =>
    Ok(views.html.lunchInfo.index())
  }

  def todaysLunches = Cached("todaysLunches", 60 * 10) {
    Action { request =>
      val todaysLunchesPromise = Akka.future { LunchInfoFetcher.fetchTodaysLunchInfo }
      Async {
        todaysLunchesPromise.map(todaysLunches => Ok(views.html.lunchInfo.todaysLunches(todaysLunches)))
      }
    }
  }

  def fetchLunchInfo = Action { request =>

    /*
    request.body.asJson.map { json =>
      (json \ "name").asOpt[String].map { name =>
        Ok("Hello " + name)
      }.getOrElse {
        BadRequest("Missing parameter [name]")
      }
    }.getOrElse {
      BadRequest("Expecting Json impl")
    }
    */

    val json = Json.toJson(
      Map("status" -> "OK")
    )

    Ok(json)

  }

  def fetchLunchInfoExample = Action { request =>
    val json = Json.toJson(
      Map("status" -> "OK")
    )

    Ok(json)
  }
}