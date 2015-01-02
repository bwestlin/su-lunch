package model

import model.LunchInfoFetcher._
import play.api.libs.json._

import scala.util.{Failure, Success}

object JsonFormats {

  implicit val mealWrt = Json.writes[Meal]
  implicit val restaurantWrt = new Writes[Restaurant] {
    def writes(restaurant: Restaurant) = {
      Json.obj(
        "id" -> restaurant.id.toString,
        "name" -> restaurant.name,
        "url" -> restaurant.url
      )
    }
  }

  implicit val restaurantWithMealsWrt = new Writes[RestaurantWithMeals] {
    def writes(restaurantWithMeals: RestaurantWithMeals) = {
      val (restaurant, mealsTry) = restaurantWithMeals
      val mealsOrError =
        mealsTry match {
          case Success(meals: Seq[Meal]) => Seq("meals" -> Json.toJson(meals))
          case Success(_) => Nil
          case Failure(e) => Seq("error" -> JsString(e.getMessage))
        }

      JsObject(Seq("restaurant" -> Json.toJson(restaurant)) ++ mealsOrError)
    }
  }

}
