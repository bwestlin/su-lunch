package model

import org.joda.time.{Period, DateTime}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.util.{Failure, Try}
import play.api.libs.ws.WS

object LunchInfoFetcher {

  import LunchInfoParser._

  /**
   * Fetch lunch info for today from all defined restaurants
   */
  def fetchTodaysLunchInfo: Future[Seq[(Restaurant, Try[Seq[Meal]])]] = {

    var todayDT: DateTime = new DateTime()
    //todayDT = todayDT.minus(Period.days(1))

    // Get parser for each restaurant
    val restaurantsWithParser = Restaurant.getAll.map { restaurant =>
      val parser = allParsers.find {
        case (name, _) => name == restaurant.parser
      }
      (restaurant, parser.map(_._2).getOrElse(null))
    }

    // Fetch lunch info from each restaurant and parse in parallel
    val futureLunchInfos = restaurantsWithParser.map {
      case (restaurant, parser) => {
        val holder = WS.url(restaurant.url)
          .withHeaders(restaurant.requestHeaders.getOrElse(Seq()):_*)
          .withRequestTimeout(10 * 1000)

        holder.get.map { response =>
          (restaurant, Try(parser.parse(todayDT, response.body)))
        } recover {
          case e => (restaurant, Failure(e))
        }
      }
    }

    Future.sequence(futureLunchInfos).map { lunchInfos =>
      // Logging for failing lunchinfo sources
      for ((resturant, mealsTry) <- lunchInfos) {
        mealsTry match {
          case Failure(ex) =>
            Logger.warn("Lunchinfo fetching for restaurant: " + resturant.name + " failed!", ex)
          case _ =>
        }
      }
      lunchInfos
    }
  }

}