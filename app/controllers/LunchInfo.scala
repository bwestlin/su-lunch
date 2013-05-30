package controllers

import play.api._
import cache.Cached
import mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import model.LunchInfoFetcher

object LunchInfo extends Controller {

  def index = Action { request =>
    val ipAddress = request.headers.get("X-Forwarded-For") match {
      case Some(ip: String) => ip
      case None => request.remoteAddress
    }
    println("Index requested from ip-address: " + ipAddress + ", request.remoteAddress=" + request.remoteAddress)

    Ok(views.html.lunchInfo.index())
  }

  def todaysLunches = Cached("todaysLunches", 60 * 10) {
    Action { request =>
      val todaysLunchesFuture = LunchInfoFetcher.fetchTodaysLunchInfo
      Async {
        todaysLunchesFuture.map(todaysLunches =>
          Ok(views.html.lunchInfo.todaysLunches(todaysLunches)).withHeaders(PRAGMA -> "no-cache")
        )
      }
    }
  }
}