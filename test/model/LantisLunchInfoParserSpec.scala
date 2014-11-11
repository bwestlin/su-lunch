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

import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import scala.xml._

@RunWith(classOf[JUnitRunner])
class LantisLunchInfoParserSpec extends Specification {

  "LantisLunchInfoParser" should {

    val parser = LantisLunchInfoParser

    def html(mealNames: Seq[String]) =
      <div class="col-md-3 hors-menu text-center">
        <h2 id="tillmenyn">Dagens lunch Mån. 10/11</h2>
        {
          mealNames.map { name =>
            <div class="row">
              <div class="col-xs-2"></div>
              <div class="col-xs-10 text-left">{name}</div>
            </div>
          }
        }
        <p class="small"></p>
      </div>

    def defaultMealNames(nMeals: Int) = (1 to nMeals).map("Meal" + _)

    "Parse menu from html correctly when correct date" in {
      val meals = parser(DateTime.parse("2014-11-10T12.00"), html(defaultMealNames(3)).toString())
      meals.length must beEqualTo(3)
      meals(0).description must beEqualTo("Svenska smaker: Meal1")
      meals(1).description must beEqualTo("World food: Meal2")
      meals(2).description must beEqualTo("Healthy: Meal3")
    }

    "At maximum parse 3 meals" in {
      val meals = parser(DateTime.parse("2014-11-10T12.00"), html(defaultMealNames(4)).toString())
      meals.length must beEqualTo(3)
    }

    "Fail to parse menu from html when dates doesn't match" in {
      val meals = parser(DateTime.parse("2014-11-11T12.00"), html(defaultMealNames(3)).toString())
      meals must beNull // TODO Not null plz
      //meals.length must beEqualTo(0)
    }

    "Fail on ureasonable html" in {
      val dt = DateTime.parse("2014-11-10T12.00")
      parser(dt, html(Seq("Måndag")).toString()) must throwAn("otillförlitligt resultat")
      parser(dt, html(Seq("måndag")).toString()) must throwAn("otillförlitligt resultat")
    }
  }
}
