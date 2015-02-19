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

package views

import model._
import org.jsoup.Jsoup
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import collection.JavaConversions._

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class TodaysLunchesSpec extends Specification {

  "TodaysLunches view" should {

    "Render list of meals correctly" in {
      val restaurant = Restaurant(1, "Restaurant", "http://restaurant/", None, "Restaurant")
      val lunches: Seq[(Restaurant, Try[Seq[model.Meal]])] = Seq(
        (restaurant, Success(Seq("meal1", "meal2").map(Meal)))
      )
      val html = views.html.lunchInfo.todaysLunches(lunches).toString()
      val doc = Jsoup.parse(html)

      doc.select("h4").text() mustEqual restaurant.name
      doc.select("h4 > a").attr("href") mustEqual restaurant.url
      doc.select("ul.meals").size() mustEqual 1
      doc.select("ul.meals > li").size() mustEqual 2
      doc.select("ul.meals > li").iterator().toSeq.map(_.text) mustEqual Seq("meal1", "meal2")
    }

    "Render empty list of meals correctly" in {
      val restaurant = Restaurant(1, "Restaurant", "http://restaurant/", None, "Restaurant")
      val lunches: Seq[(Restaurant, Try[Seq[model.Meal]])] = Seq(
        (restaurant, Success(Nil))
      )
      val html = views.html.lunchInfo.todaysLunches(lunches).toString()
      val doc = Jsoup.parse(html)

      doc.select("h4").text() mustEqual restaurant.name
      doc.select("h4 > a").attr("href") mustEqual restaurant.url
      doc.select("div:containsOwn(Inga luncher funna.)").size() mustEqual 1
    }

    "Render failed list of meals correctly" in {
      val restaurant = Restaurant(1, "Restaurant", "http://restaurant/", None, "Restaurant")
      val lunches: Seq[(Restaurant, Try[Seq[model.Meal]])] = Seq(
        (restaurant, Failure(new Exception("Something went wrong")))
      )
      val html = views.html.lunchInfo.todaysLunches(lunches).toString()
      val doc = Jsoup.parse(html)

      doc.select("h4").text() mustEqual restaurant.name
      doc.select("h4 > a").attr("href") mustEqual restaurant.url
      doc.select("div.alert.alert-error").size mustEqual 1
      doc.select("div.alert.alert-error").text must contain("Something went wrong")
    }
  }
}
