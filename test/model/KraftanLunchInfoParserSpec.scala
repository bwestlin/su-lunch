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
      <div class="post-content no-thumbnail">
        <div class="post-info top">
          <span class="post-type-icon-wrap"><span class="post-type-icon"></span></span>
          <span class="post-date">
            16 november, 2014
          </span>
          <span class="no-caps post-autor">
            &nbsp;by  <a href="http://www.kraftan.nu/author/kraftan/" title="Inlägg av kraftan" rel="author">kraftan</a>			</span>
        </div>
        <div class="post-title-wrapper">
          <h2 class="post-title">
            <a href="http://www.kraftan.nu/menyer/lunchmeny-v-47-2/">Lunchmeny v.47</a>
          </h2>
        </div>
        <div class="clear"></div>
        <div class="post-content-content">
          <p><em>Måndag</em></p>
          <p><em>mon-{mealName}1</em></p>
          <p><em>**</em></p>
          <p><em>mon-{mealName}2</em></p>
          <p><em>&nbsp;</em></p>
          <p><em>Tisdag </em></p>
          <p><em>tue-{mealName}1</em></p>
          <p><em>**</em></p>
          <p><em>tue-{mealName}2</em></p>
          <p><em>&nbsp;</em></p>
          <p><em>Onsdag</em></p>
          <p><em>wed-{mealName}1</em></p>
          <p><em>**</em></p>
          <p><em>wed-{mealName}2</em></p>
          <p><em>&nbsp;</em></p>
          <p><em>Torsdag</em></p>
          <p><em>thu-{mealName}1</em></p>
          <p><em>**</em></p>
          <p><em>thu-{mealName}2</em></p>
          <p><em>&nbsp;</em></p>
          <p><em>&nbsp;</em><em>Fredag</em></p>
          <p><em>fri-{mealName}1</em></p>
          <p><em>**</em></p>
          <p><em>fri-{mealName}2</em></p>
          <p><em>&nbsp;</em></p>
          <p><em>foo bar</em></p>
          <div class="clear"></div>
          <div class="post-info bottom">
            <span class="post-type-icon-wrap"><span class="post-type-icon"></span></span>
            <span class="no-caps">
              in			</span><a href="http://www.kraftan.nu/category/menyer/" rel="category tag">Menyer</a>
            <span class="comments-number">
              <a href="http://www.kraftan.nu/menyer/lunchmeny-v-47-2/#comments">
                0				<span class="no-caps">comments</span></a>
            </span>
          </div>
          <div class="clear"></div>
        </div>
      </div>

    val weekdays = Seq("mon", "tue", "wed", "thu", "fri")

    "Parse menu from html correctly when correct date in week" in {
      for (dayIdx <- 0 to 4) yield {
        val meals = parser(DateTime.parse("2014-11-17T12.00").plus(Period.days(dayIdx)), html().toString())
        meals.length must beEqualTo(2)
        meals(0).description must beEqualTo(weekdays(dayIdx) + "-meal1")
        meals(1).description must beEqualTo(weekdays(dayIdx) + "-meal2")
      }
      true
    }

    "Fail to parse menu from html when dates doesn't match" in {
      val meals = parser(DateTime.parse("2014-11-10T12.00"), html().toString())
      meals must beNull // TODO Not null plz
      //meals.length must beEqualTo(0)
    }

    "Fail on ureasonable html" in {
      val dt = DateTime.parse("2014-11-17T12.00")
      parser(dt, html("Måndag").toString()) must throwAn("otillförlitligt resultat")
      parser(dt, html("måndag").toString()) must throwAn("otillförlitligt resultat")
    }
  }
}
