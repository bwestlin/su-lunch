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

import org.joda.time.{Period, DateTime}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import scala.xml._

@RunWith(classOf[JUnitRunner])
class FossilenLunchInfoParserSpec extends Specification {

  "FossilenLunchInfoParser" should {

    val parser = FossilenLunchInfoParser

    def html(mealName: String = "meal") =
      <div class="sv-text-portlet-content">
        <h2 class="h2" id="h-Menyvecka7915februari">Meny vecka 7,&nbsp;9-15 februari</h2>
        <h3 class="h3" id="h-Mandag">Måndag</h3>
        <p class="brodtext">mon-{mealName}1&nbsp;</p>
        <p class="brodtext">mon-{mealName}2</p>
        <p class="brodtext">mon-{mealName}3&nbsp;&nbsp; </p>
        <p class="brodtext">mon-{mealName}4</p>
        <h3 class="h3" id="h-Tisdag">Tisdag</h3>
        <p class="brodtext">tue-{mealName}1</p>
        <p class="brodtext">tue-{mealName}2</p>
        <p class="brodtext">tue-{mealName}3</p>
        <p class="brodtext">tue-{mealName}4</p>
        <h3 class="h3" id="h-Onsdag">Onsdag</h3>
        <p class="brodtext">wed-{mealName}1</p>
        <p class="brodtext">wed-{mealName}2</p>
        <p class="brodtext">wed-{mealName}3</p>
        <p class="brodtext">wed-{mealName}4</p>
        <h3 class="h3" id="h-Torsdag">Torsdag</h3>
        <p class="brodtext">thu-{mealName}1&nbsp;</p>
        <p class="brodtext">thu-{mealName}2&nbsp;</p>
        <p class="brodtext">thu-{mealName}3&nbsp;&nbsp; </p>
        <p class="brodtext">thu-{mealName}4&nbsp;</p>
        <h3 class="h3" id="h-Fredag">Fredag</h3>
        <p class="brodtext">fri-{mealName}1</p>
        <p class="brodtext">fri-{mealName}2</p>
        <p class="brodtext">fri-{mealName}3</p>
        <p class="brodtext">fri-{mealName}4</p>
        <h3 class="h3" id="h-Lordagsondag">Lördag, söndag</h3>
        <p class="brodtext">
          sunsat-{mealName}1 <br/><br/>
          sunsat-{mealName}2 <br/><br/>
          sunsat-{mealName}3 <br/><br/>
          sunsat-{mealName}4
        </p>
      </div>

    val weekdays = Seq("mon", "tue", "wed", "thu", "fri", "sunsat", "sunsat")

    "Parse menu from html correctly when correct date in week" in {
      for (dayIdx <- 0 to 6) yield {
        val meals = parser(DateTime.parse("2015-02-09T12.00").plus(Period.days(dayIdx)), html().toString())
        meals.length must beEqualTo(4)
        meals(0).description must beEqualTo(weekdays(dayIdx) + "-meal1")
        meals(1).description must beEqualTo(weekdays(dayIdx) + "-meal2")
        meals(2).description must beEqualTo(weekdays(dayIdx) + "-meal3")
        meals(3).description must beEqualTo(weekdays(dayIdx) + "-meal4")
      }
      true
    }

    "Fail to parse menu from html when dates doesn't match" in {
      val meals = parser(DateTime.parse("2014-11-10T12.00"), html().toString())
      meals must beNull // TODO Not null plz
      //meals.length must beEqualTo(0)
    }

    "Fail on ureasonable html" in {
      val dt = DateTime.parse("2015-02-09T12.00")
      parser(dt, html("Måndag").toString()) must throwAn("otillförlitligt resultat")
      parser(dt, html("måndag").toString()) must throwAn("otillförlitligt resultat")
    }
  }
}
