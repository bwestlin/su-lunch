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

package model.fetchers

import model._
import collection.JavaConversions._
import scala.util.{Failure, Try}
import scala.concurrent.Future
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import org.jsoup._
import nodes.Element
import org.joda.time.{DateTime, Period}

object LunchInfoFetcher {

  /**
   * A map with all LunchInfoFetchers mapped to their corresponding name
   */
  lazy val allFetchers: Map[String, LunchInfoFetcher] = {
    import scala.reflect.runtime._

    val lunchInfoFetcherClass = classOf[LunchInfoFetcher]
    val rootMirror = universe.runtimeMirror(lunchInfoFetcherClass.getClassLoader)
    var lunchInfoFetcherClassSymbol = rootMirror.classSymbol(lunchInfoFetcherClass)

    // For some unknown reason this has to be done to make it work
    rootMirror.reflectClass(lunchInfoFetcherClassSymbol)

    // Find all subclass singletons of class LunchInfoFetcher
    lunchInfoFetcherClassSymbol.knownDirectSubclasses.flatMap { symbol =>
      if (symbol.isModuleClass) {
        val moduleMirror = rootMirror.reflectModule(symbol.companionSymbol.asModule)
        val name = symbol.name.toString.dropRight(lunchInfoFetcherClassSymbol.name.toString.length)
        val instance = moduleMirror.instance.asInstanceOf[LunchInfoFetcher]
        Option((name, instance))
      }
      else None
    }.toMap
  }

  /**
   * Fetch lunch info for today from all defined restaurants
   */
  def fetchTodaysLunchInfo: Future[Seq[(Restaurant, Try[Seq[Meal]])]] = {

    var todayDT: DateTime = new DateTime()
    //todayDT = todayDT.minus(Period.days(1))

    // Get fetcher for each restaurant
    val restaurantsToFetch = Restaurant.getAll.map { restaurant =>
      val fetcher = allFetchers.find {
        case (name, _) => name == restaurant.fetcher
      }
      (restaurant, fetcher.map(_._2).getOrElse(null))
    }

    // Fetch lunch info from each restaurant in parallel
    val futureLunchInfos = restaurantsToFetch.map {
      case (restaurant, fetcher) => Future {
        (restaurant, Try(fetcher(todayDT, restaurant.url)))
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

/**
 * Base class for functions to fetch lunches for restaurants.
 * Currently it has to be sealed to make allFetchers work since knownDirectSubclasses only works for sealed classes.
 */
sealed abstract class LunchInfoFetcher {

  def apply(day: DateTime, url: String): Seq[Meal] = {
    val meals = fetch(day, url)

    if (meals != null && mealResultValidators.exists(_(meals)))
      throw new Exception("Inhämtningen gav ett otillförlitligt resultat")

    meals
  }

  def fetch(day: DateTime, url: String): Seq[Meal]

  def mealResultValidators: Seq[(Seq[Meal]) => Boolean] = Seq(
    { meals =>
      meals.size >= 10
    },
    { meals =>
      meals.exists { meal =>
        weekdays.exists(weekday => meal.description.contains(weekday) || meal.description.contains(weekday.toLowerCase))
      }
    }
  )

  def weekdays = List("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag")

  def months = List("Januari", "Februari", "Mars", "April", "Maj", "Juni", "Juli", "Augusti", "September", "Oktober", "November", "December")
}

/**
 * A function to fetch all lunches for a given day from restaurant Lantis, see http://www.hors.se/restaurang-lantis
 */
object LantisLunchInfoFetcher extends LunchInfoFetcher {

  override def fetch(dayDT: DateTime, url: String): Seq[Meal] = {
    val doc = Jsoup.connect(url)
      .timeout(10 * 1000)
      .get()

    val lunchmenulist = doc.select(".lunchmenulist").first

    val weekdays = this.weekdays.map(_.toLowerCase)
    val weekday = weekdays(dayDT.dayOfWeek.get - 1)
    if (lunchmenulist != null && lunchmenulist.select("h2:containsOwn(" + weekday + ")").first != null) {

      val typMap = Map(
        "kott" -> "Kött",
        "fisk" -> "Fisk",
        "veg"  -> "Vegetarisk"
      ).withDefaultValue("?")

      lunchmenulist.select(".lunchmenulisttext").map { elem =>
        val typ = elem.parent.className
        Meal(typMap(typ) + ": " + elem.text)
      }
    } else null
  }
}

/**
 * A function to fetch all lunches for a given day from restaurant Fossilen, see http://nrm.se/besokmuseet/restaurangfossilen
 */
object FossilenLunchInfoFetcher extends LunchInfoFetcher {

  override def fetch(dayDT: DateTime, url: String): Seq[Meal] = {
    val doc = Jsoup.connect(url)
      .timeout(10 * 1000)
      .get()

    val weekStartDate = dayDT.withDayOfWeek(1)
    val months = this.months.map(_.toLowerCase)
    val weekStartMonth = months(weekStartDate.monthOfYear.get - 1)

    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

    val baseElement = doc.select(".sv-text-portlet-content")

    // Check that the webpage consist of the right week according to today
    val correctWeek = Option(baseElement.select("h2").first).map { headerElem =>
      headerElem.text.split(Array(',', '-', ' ')).map(_.trim).toList match {
        case _ :: _ :: _ :: _ :: weekStartDay :: _ :: month :: Nil
          if weekStartDay.toInt == weekStartDate.dayOfMonth.get && month == weekStartMonth => true
        case _ => false
      }
    }

    val dayH3 = baseElement.select("h3:containsOwn(" + weekday + ")").first

    if (correctWeek.getOrElse(false) && dayH3 != null) {
      val next = dayH3.nextElementSibling

      def splitByCapitalLetters(text: String): List[String] = {
        if (text == null || text.length == 0) List()
        else {
          val capitalLetterPart = text.take(1) + text.drop(1).takeWhile(!_.isUpper)
          capitalLetterPart :: splitByCapitalLetters(text.drop(capitalLetterPart.length))
        }
      }

      def getMeals(nextElem: Element): List[Meal] = {
        if (nextElem == null || nextElem.tagName != "p") List()
        else {
          splitByCapitalLetters(nextElem.text).map { text =>
            Meal(text.trim)
          } ::: getMeals(nextElem.nextElementSibling)
        }
      }
      getMeals(next)
    }
    else null
  }
}

/**
 * A function to fetch all lunches for a given day from restaurant Stora Skuggan, see http://gastrogate.com/restaurang/storaskuggan/page/3
 */
object StoraSkugganLunchInfoFetcher extends LunchInfoFetcher {

  override def fetch(dayDT: DateTime, url: String): Seq[Meal] = {
    val doc = Jsoup.connect(url)
      .timeout(10 * 1000)
      .ignoreHttpErrors(true)
      .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Ubuntu Chromium/25.0.1364.160 Chrome/25.0.1364.160 Safari/537.22")
      .get()

    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

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
    }
    else null
  }
}

/**
 * A function to fetch all lunches for a given day from restaurant Kräftan, see http://www.kraftan.nu/
 */
object KraftanLunchInfoFetcher extends LunchInfoFetcher {

  override def fetch(dayDT: DateTime, url: String): Seq[Meal] = {
    val doc = Jsoup.connect(url)
      .timeout(10 * 1000)
      .get()

    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

    val dayP = doc.select("#about p:contains(" + weekday + ")").first
    if (dayP != null) {

      def getLunches(p: Element): List[Meal] = {
        val text = if (p != null) p.text.trim else null
        if (text == null || text.length <= 1 || weekdays.contains(text)) List()
        else if (text == "**")
          getLunches(p.nextElementSibling)
        else {
          Meal(text) :: getLunches(p.nextElementSibling)
        }
      }

      getLunches(dayP.nextElementSibling)
    }
    else null
  }
}
