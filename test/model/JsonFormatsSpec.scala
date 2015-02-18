/*
 * Copyright 2013-, Björn Westlin (bwestlin at gmail dot com) - github: bwestlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package model

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.Json
import scala.util.{Failure, Success, Try}
import JsonFormats._

@RunWith(classOf[JUnitRunner])
class JsonFormatsSpec extends Specification {

  "JsonFormats" should {

    "Generate correct json for restaurants and meals" in {
      val restaurant = Restaurant(1, "Restaurant", "http://restaurant/", None, "Restaurant")
      val lunches: Seq[(Restaurant, Try[Seq[model.Meal]])] = Seq(
        (restaurant, Success(Seq("meal1", "meal2").map(Meal)))
      )

      Json.toJson(lunches).toString mustEqual "[{\"restaurant\":{\"id\":\"1\",\"name\":\"Restaurant\",\"url\":\"http://restaurant/\"},\"meals\":[{\"description\":\"meal1\"},{\"description\":\"meal2\"}]}]"
    }

    "Generate correct json for restaurants and no meals" in {
      val restaurant = Restaurant(1, "Restaurant", "http://restaurant/", None, "Restaurant")
      val lunches: Seq[(Restaurant, Try[Seq[model.Meal]])] = Seq(
        (restaurant, Success(null))
      )

      Json.toJson(lunches).toString mustEqual "[{\"restaurant\":{\"id\":\"1\",\"name\":\"Restaurant\",\"url\":\"http://restaurant/\"}}]"
    }

    "Generate correct json for restaurants and failed meals" in {
      val restaurant = Restaurant(1, "Restaurant", "http://restaurant/", None, "Restaurant")
      val lunches: Seq[(Restaurant, Try[Seq[model.Meal]])] = Seq(
        (restaurant, Failure(new Exception("Something went wrong")))
      )

      Json.toJson(lunches).toString mustEqual "[{\"restaurant\":{\"id\":\"1\",\"name\":\"Restaurant\",\"url\":\"http://restaurant/\"},\"error\":\"Something went wrong\"}]"
    }
  }
}
