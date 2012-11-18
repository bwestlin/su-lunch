package controllers

import play.api._
import cache.Cached
import play.api.Play.current
import libs.concurrent.Akka
import mvc._

import impl.lunchInfo._

object LunchInfo extends Controller {

  def index = Action { request =>
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