package model

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.util.{Failure, Try}
import play.api.libs.ws.{WSResponse, WS}
import play.api.Play.current

import LunchInfoParser._
import LunchInfoFetcher._


object LunchInfoFetcher {

  type RestaurantWithParser = (Restaurant, LunchInfoParser)
  type RestaurantWithMeals  = (Restaurant, Try[Seq[Meal]])

  def apply() = new LunchInfoFetcher
}

class LunchInfoFetcher {

  /**
   * Get parser for each defined restaurant
   */
  def allRestaurantsWithParser: Seq[RestaurantWithParser] = Restaurant.getAll.map { restaurant =>
    val maybeParser = getParser(restaurant.parser)
    maybeParser.map { parser =>
      (restaurant, parser)
    }
  }.flatten

  /**
   * Fetch data using GET request for a given url
   */
  def fetchUrl(url: String, requestHeaders: Option[Map[String, String]] = None, timeoutSec: Int = 10): Future[WSResponse] = {
    WS.url(url)
      .withHeaders(requestHeaders.map(_.toSeq).getOrElse(Nil): _*)
      .withRequestTimeout(timeoutSec * 1000)
      .get()
  }

  /**
   * Fetch lunch info for today from all defined restaurants
   */
  def fetchTodaysLunchInfo(todayDT: DateTime = DateTime.now(),
                           restaurantsWithParser: Seq[RestaurantWithParser] = allRestaurantsWithParser): Future[Seq[RestaurantWithMeals]] = {

    // Fetch lunch info from each restaurant and parse in parallel
    val futureLunchInfos = restaurantsWithParser.map { case (restaurant, parser) =>

      fetchUrl(restaurant.url, restaurant.requestHeaders).map { response =>
        (restaurant, Try(parser(todayDT, response.body)))
      } recover {
        case e => (restaurant, Failure(e))
      }
    }

    Future.sequence(futureLunchInfos).map { lunchInfos =>
      // Logging for failing lunchinfo sources
      for ((resturant, mealsTry) <- lunchInfos) {
        mealsTry match {
          case Failure(ex) =>
            Logger.warn(s"Lunchinfo fetching for restaurant: ${resturant.name} failed!", ex)
          case _ =>
        }
      }
      lunchInfos
    }
  }
}
