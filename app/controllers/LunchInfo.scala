package controllers

import play.api._
import cache.Cached
import play.api.Play.current
import libs.concurrent.Akka
import mvc._

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
      val todaysLunchesPromise = Akka.future { LunchInfoFetcher.fetchTodaysLunchInfo }
      Async {
        todaysLunchesPromise.map(todaysLunches =>
          Ok(views.html.lunchInfo.todaysLunches(todaysLunches)).withHeaders(PRAGMA -> "no-cache")
        )
      }
    }
  }
}