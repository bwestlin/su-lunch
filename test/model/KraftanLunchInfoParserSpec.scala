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
class KraftanLunchInfoParserSpec extends Specification {

  "KraftanLunchInfoParser" should {

    val parser = KraftanLunchInfoParser

    def html(mealName: String = "meal") =
      <div class="content-box">
        <p><strong>Lunchmeny v.46</strong></p>
        <div class="cols-wrapper cols-3">
          <div class="col">
            <p>&nbsp;<strong>Måndag</strong></p>
            <p>mon-{mealName}1<br />
              **<br />
              mon-{mealName}2</p>
            <p><strong>Tisdag</strong><br />
              tue-{mealName}1<br />
              **<br />
              tue-{mealName}2</p>
          </div>
          <div class="col">
            <p>&nbsp;<strong>Onsdag</strong></p>
            <p>wed-{mealName}1<br />
              **<br />
              wed-{mealName}2</p>
            <p><strong>Torsdag</strong><br />
              thu-{mealName}1<br />
              **<br />
              thu-{mealName}2</p>
          </div>
          <div class="col nomargin">
            <p>&nbsp;<strong>Fredag</strong></p>
            <p>fri-meal1<br />
              **<br />
              fri-meal2</p>
            <p><em>foo</em></p>
          </div>
        </div>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <div class="clear"></div>
      </div>

    val weekdays = Seq("mon", "tue", "wed", "thu", "fri")

    "Parse menu from html correctly when correct date in week" in {
      for (dayIdx <- 0 to 4) yield {
        val meals = parser(DateTime.parse("2014-11-10T12.00").plus(Period.days(dayIdx)), html().toString())
        meals.length must beEqualTo(2)
        meals(0).description must beEqualTo(weekdays(dayIdx) + "-meal1")
        meals(1).description must beEqualTo(weekdays(dayIdx) + "-meal2")
      }
    }

    "Fail to parse menu from html when dates doesn't match" in {
      val meals = parser(DateTime.parse("2014-11-03T12.00"), html().toString())
      meals must beNull // TODO Not null plz
      //meals.length must beEqualTo(0)
    }

    "Fail on ureasonable html" in {
      val dt = DateTime.parse("2014-11-10T12.00")
      parser(dt, html("Måndag").toString()) must throwAn("otillförlitligt resultat")
      parser(dt, html("måndag").toString()) must throwAn("otillförlitligt resultat")
    }
  }
}
