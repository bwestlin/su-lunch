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

import collection.JavaConversions._

import org.jsoup._
import nodes.Element
import org.joda.time.{DateTime, Period}
import scala.util.Try
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object LunchInfoFetcher {

  def fetchTodaysLunchInfo: Future[Seq[(Restaurant, Try[Seq[Meal]])]] = {

    type RestaurantFetcher = (String) => Seq[Meal]

    var todayDT: DateTime = new DateTime()
    //todayDT = todayDT.minus(Period.days(1))

    val restaurantsToFetch: List[(String, String, RestaurantFetcher)] = List(
      (
        "Restaurang Lantis",
        "http://www.hors.se/restaurang-lantis",
        url => {
          val doc = Jsoup.connect(url).timeout(10*1000).get()

          val lunchmenulist = doc.select(".lunchmenulist").first

          val weekdays = List("måndag", "tisdag", "onsdag", "torsdag", "fredag", "lördag", "söndag")
          val weekday = weekdays(todayDT.dayOfWeek.get - 1)
          if (lunchmenulist.select("h2:containsOwn(" + weekday + ")").first != null) {

            val typMap = Map(
              ("kott" -> "Kött"),
              ("fisk" -> "Fisk"),
              ("veg" -> "Vegetarisk")
            )

            lunchmenulist.select(".lunchmenulisttext").map(elem => {
              val typ = elem.parent.className
              Meal(typMap(typ) + ": " + elem.text)
            })
          } else null
        }
      ),
      (
        "Stora Skuggans Wärdshus",
        "http://gastrogate.com/restaurang/storaskuggan/page/3",
        url => {
          val doc = Jsoup.connect(url)
                         .timeout(10*1000)
                         .ignoreHttpErrors(true)
                         .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Ubuntu Chromium/25.0.1364.160 Chrome/25.0.1364.160 Safari/537.22")
                         .get()

          val weekdays = List("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag")
          val weekday = weekdays(todayDT.dayOfWeek.get - 1)

          //val dayTd = doc.select("div.restcontent").select("td:contains(" + weekday + ")").first
          val dayTd = doc.select("td:containsOwn(" + weekday + ")").first
          if (dayTd != null) {
            //val next = dayTd.parent.nextElementSibling
            val next = dayTd.parent.nextElementSibling.nextElementSibling

            def getRows(row: Element): List[Meal] = {
              val tds = if (row != null) row.select("td") else null
              val firstTd = if (tds != null) tds.first else null

              //if (firstTd == null || firstTd.attr("colspan") == "3") List()
              if (firstTd == null || firstTd.className == "rubriksmaller") List()
              else {
                Meal(row.text) :: getRows(row.nextElementSibling)
              }
            }

            getRows(next)
          } else null
        }
      ),
      (
        "Värdshuset Kräftan",
        "http://www.kraftan.nu/",
        url => {
          val doc = Jsoup.connect(url).timeout(10*1000).get()

          val weekdays = List("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag")
          val weekday = weekdays(todayDT.dayOfWeek.get - 1)

          val dayP = doc.select("#about p:contains(" + weekday + ")").first
          if (dayP != null) {

            def getLunches(p: Element): List[Meal] = {
              val text = if (p != null) p.text.trim else null
              if (text == null || text.length <= 1) List()
              else if (text == "**")
                getLunches(p.nextElementSibling)
              else {
                Meal(text) :: getLunches(p.nextElementSibling)
              }
            }

            getLunches(dayP.nextElementSibling)
          } else null
        }
      )
    )

    // Fetch lunches from each restaurant in parallel
    val futureLunchInfos = restaurantsToFetch.map {
      case (name, url, fetcher) => Future {
        (Restaurant(name, url), Try(fetcher(url)))
      }
    }

    Future.sequence(futureLunchInfos)
  }

}
