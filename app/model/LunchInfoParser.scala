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
import org.joda.time.DateTime
import common.JsoupExtensions._
import common.StringExtensions._

object LunchInfoParser {

  /**
   * A map with all LunchInfoParsers mapped to their corresponding name
   */
  lazy val allParsers: Map[String, LunchInfoParser] = {
    import scala.reflect.runtime._

    val lunchInfoParserClass = classOf[LunchInfoParser]
    val rootMirror = universe.runtimeMirror(lunchInfoParserClass.getClassLoader)
    var lunchInfoParserClassSymbol = rootMirror.classSymbol(lunchInfoParserClass)

    // For some unknown reason this has to be done to make it work
    rootMirror.reflectClass(lunchInfoParserClassSymbol)

    // Find all subclass singletons of class LunchInfoParser
    lunchInfoParserClassSymbol.knownDirectSubclasses.flatMap { symbol =>
      if (symbol.isModuleClass) {
        val moduleMirror = rootMirror.reflectModule(symbol.companionSymbol.asModule)
        val name = symbol.name.toString.dropRight(lunchInfoParserClassSymbol.name.toString.length)
        val instance = moduleMirror.instance.asInstanceOf[LunchInfoParser]
        Option((name, instance))
      }
      else None
    }.toMap
  }
}

/**
 * Base class for functions to fetch lunches for restaurants.
 * Currently it has to be sealed to make LunchInfoFetcher.allFetchers to work since knownDirectSubclasses
 * only works for sealed classes. Which also has the consequence that the objects extending this class has to
 * be in the same file.
 */
sealed abstract class LunchInfoParser {

  def apply(day: DateTime, body: String): Seq[Meal] = {
    val meals = parse(day, body)

    if (meals != null && isMealResultUnreasonable(meals))
      throw new Exception("Inhämtningen gav ett otillförlitligt resultat")

    meals
  }

  protected def parse(day: DateTime, body: String): Seq[Meal]

  protected def isMealResultUnreasonable(meals: Seq[Meal]) = {
    meals.size >= 10 || meals.exists { meal =>
      weekdays.exists(weekday => meal.description.contains(weekday) || meal.description.contains(weekday.toLowerCase))
    }
  }

  protected def weekdays = List("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag")
  protected def weekdaysShort = List("Mån", "Tis", "Ons", "Tors", "Fre", "Lör", "Sön")

  protected def months = List("Januari", "Februari", "Mars", "April", "Maj", "Juni", "Juli", "Augusti", "September", "Oktober", "November", "December")
}

/**
 * Function to parse all lunches for a given day from restaurant Lantis, see http://www.hors.se/restaurang-lantis
 */
object LantisLunchInfoParser extends LunchInfoParser {

  override protected def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val lunchmenulist = doc.select(".hors-menu").first
    val weekday = weekdaysShort(dayDT.dayOfWeek.get - 1)
    val title = "Dagens lunch " + weekday + ". " + dayDT.toString("dd/MM")

    if (lunchmenulist != null && lunchmenulist.select("h2:containsOwn(" + title + ")").first != null) {
      val types = List("Svenska smaker", "World food", "Healthy")

      lunchmenulist.select(".row .text-left").zip(types).map { elem =>
        Meal(elem._2 + ": " + elem._1.text)
      }
    } else null
  }
}

/**
 * Function to parse all lunches for a given day from restaurant Fossilen, see http://nrm.se/besokmuseet/restaurangfossilen
 */
object FossilenLunchInfoParser extends LunchInfoParser {

  override protected def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val weekStartDate = dayDT.withDayOfWeek(1)
    val currentWeek = weekStartDate.weekOfWeekyear().get()

    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

    val baseElement = doc.select(".sv-text-portlet-content")

    // Check that the webpage consist of the right week according to today
    val correctWeek = Option(baseElement.select("h2:containsOwn(Meny)").first).map { headerElem =>
      headerElem.text.split(Array(',', '-', ' ')).map(_.trim).toList match {
        case _ :: _ :: week :: _ if week.toIntOpt.contains(currentWeek) => true
        case _ => false
      }
    }

    val dayH3 = baseElement.select("h3:containsOwn(" + weekday + ")").first

    if (correctWeek.getOrElse(false) && dayH3 != null) {
      val next = dayH3.nextElementSibling

      /*
      This has to be thought out a little better
      def splitByCapitalLetters(text: String): List[String] = {
        if (text == null || text.length == 0) List()
        else {
          val capitalLetterPart = text.take(1) + text.drop(1).takeWhile(!_.isUpper)
          capitalLetterPart :: splitByCapitalLetters(text.drop(capitalLetterPart.length))
        }
      }
      */

      def getMeals(nextElem: Element): List[Meal] = {
        if (nextElem == null || nextElem.tagName != "p") List()
        else {
          // Split meals by html breaking newlines
          val meals = for {
            mealByBr <- nextElem.html.split("<br />").map(Jsoup.parse(_).text()).toList
          } yield Meal(mealByBr)

          meals ::: getMeals(nextElem.nextElementSibling)
        }
      }
      getMeals(next)
    }
    else null
  }
}

/**
 * Function to parse all lunches for a given day from restaurant Stora Skuggan, see http://gastrogate.com/restaurang/storaskuggan/page/3
 */
object StoraSkugganLunchInfoParser extends LunchInfoParser {

  override protected def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

    val dayTd = doc.select("td:containsOwn(" + weekday + ")").first
    if (dayTd != null) {
      val next = dayTd.parent.nextElementSibling

      def getRows(row: Element): List[Meal] = {
        val tds = if (row != null) row.select("td") else null
        val firstTd = if (tds != null) tds.first else null

        if (firstTd == null || firstTd.className == "menu_header") List()
        else {
          Meal(firstTd.text) :: getRows(row.nextElementSibling)
        }
      }

      getRows(next)
    }
    else null
  }
}

/**
 * Function to parse all lunches for a given day from restaurant Kräftan, see http://www.kraftan.nu/
 */
object KraftanLunchInfoParser extends LunchInfoParser {

  override protected def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val weekNum = dayDT.toString("w")
    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

    val maybeMeals =
      for {
        weekTitleElem     <- doc.select(s"h2:contains(v.$weekNum)").firstOpt
        weekdaysContainer <- weekTitleElem.parent.parent.select(s"p:contains(${weekdays.head})").firstOpt.map(_.parent)
        weekDayElem       <- weekdaysContainer.select(s"p:contains($weekday)").firstOpt
        mealsForDay       <- Option(weekDayElem.nextElementSiblings(3).map(_.text).filterNot(_ == "**"))
      } yield {
        mealsForDay.map(Meal.apply)
      }

    maybeMeals.orNull
  }

  override protected def isMealResultUnreasonable(meals: Seq[Meal]): Boolean = {
    super.isMealResultUnreasonable(meals) || meals.length > 2
  }
}
