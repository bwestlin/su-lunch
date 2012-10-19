package controllers

import play.api._
import mvc._
import play.api.libs.json._

import org.jsoup.nodes._
import org.jsoup.select._
import org.jsoup._
import java.net.URL

import impl.lunchInfo._

object LunchInfo extends Controller {

  def index = Action { request =>
    Ok(views.html.lunchInfo.index())
  }

  def todaysLunches = Action { request =>
    Ok(views.html.lunchInfo.todaysLunches(LunchInfoFetcher.fetchTodaysLunchInfo))
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

    val doc = Jsoup.connect("").get();
    //doc.

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