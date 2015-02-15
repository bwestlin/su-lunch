package model

import org.joda.time.{Period, DateTime}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.util.{Failure, Try}
import play.api.libs.ws.WS
import play.api.Play.current

object LunchInfoFetcher {

  type RestaurantWithParser = (Restaurant, LunchInfoParser)
  type RestaurantWithMeals  = (Restaurant, Try[Seq[Meal]])

  def apply() = new LunchInfoFetcher
}

class LunchInfoFetcher {

  import LunchInfoParser._
  import LunchInfoFetcher._

  /**
   * Get parser for each defined restaurant
   */
  def allRestaurantsWithParser: Seq[RestaurantWithParser] = Restaurant.getAll.map { restaurant =>
    val maybeParser = allParsers.find { case (name, _) =>
      name == restaurant.parser
    }
    maybeParser.map { case (_, parser) =>
      (restaurant, parser)
    }
  }.flatten

  /**
   * Fetch lunch info for today from all defined restaurants
   */
  def fetchTodaysLunchInfo(restaurantsWithParser: Seq[RestaurantWithParser] = allRestaurantsWithParser): Future[Seq[RestaurantWithMeals]] = {

    val todayDT = DateTime.now()

    // Fetch lunch info from each restaurant and parse in parallel
    val futureLunchInfos = restaurantsWithParser.map { case (restaurant, parser) =>

      val holder = WS.url(restaurant.url)
        .withHeaders(restaurant.requestHeaders.getOrElse(Nil): _*)
        .withRequestTimeout(10 * 1000)

      holder.get().map { response =>
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
