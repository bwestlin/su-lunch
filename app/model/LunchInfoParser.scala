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
    val lunchInfoParserClassSymbol = rootMirror.classSymbol(lunchInfoParserClass)

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

  def getParser(name: String): Option[LunchInfoParser] = {
    allParsers
      .find { case (parserName, _) =>
        parserName == name
      }
      .map { case (_, parser) =>
        parser
      }
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

    if (Option(meals).nonEmpty && isMealResultUnreasonable(meals))
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
}

/**
 * Function to parse all lunches for a given day from restaurant Lantis, see http://www.hors.se/restaurang-lantis
 */
object LantisLunchInfoParser extends LunchInfoParser {

  override protected def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val maybeLunchmenulist = doc.select(".hors-menu").firstOpt
    val weekday = weekdaysShort(dayDT.dayOfWeek.get - 1)
    val title = "Dagens lunch " + weekday + ". " + dayDT.toString("dd/M")

    val maybeLunches =
      for {
        lunchmenulist <- maybeLunchmenulist
        _             <- lunchmenulist.select("h2:containsOwn(" + title + ")").firstOpt
      } yield {
        val types = List("Svenska smaker", "World food", "Healthy")

        lunchmenulist.select(".row .text-left").zip(types).map { elem =>
          Meal(elem._2 + ": " + elem._1.text)
        }
      }

    maybeLunches.getOrElse(Nil)
  }
}

/**
 * Function to parse lunch for a given day from restaurant Biofood, see http://www.hors.se/restaurang/jalla-su/
 */
object BiofoodLunchInfoParser extends LunchInfoParser {

  override protected def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val maybeLunchmenulist = doc.select(".hors-menu").firstOpt
    val weekday = weekdaysShort(dayDT.dayOfWeek.get - 1)
    val title = "Dagens lunch " + weekday + ". " + dayDT.toString("dd/M")

    val maybeLunches =
      for {
        lunchmenulist <- maybeLunchmenulist
        _             <- lunchmenulist.select("h2:containsOwn(" + title + ")").firstOpt
      } yield {
        lunchmenulist.select(".row .text-left").map { elem =>
          Meal(elem.text)
        }
      }

    maybeLunches.getOrElse(Nil)
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
    val maybeCorrectWeek = baseElement.select("h2:containsOwn(Meny)").firstOpt.flatMap { headerElem =>
      headerElem.text.split(Array(',', '-', ' ')).map(_.trim).toList match {
        case _ :: _ :: week :: _ if week.toIntOpt.contains(currentWeek) => Some(week)
        case _ => None
      }
    }

    val maybeLunches =
      for {
        _     <- maybeCorrectWeek
        dayH3 <- baseElement.select("h3:containsOwn(" + weekday + ")").firstOpt
      } yield {
        val next = Option(dayH3.nextElementSibling)

        def getMeals(nextElem: Option[Element]): List[Meal] = nextElem match {
          case None => Nil
          case Some(elem) if elem.tagName != "p" => Nil
          case Some(elem) => {
            // Split meals by html breaking newlines
            val meals = for {
              mealByBr <- elem.html.split("<br />").map(Jsoup.parse(_).text().trimWhitespace).toList
              if mealByBr.nonEmpty
            } yield Meal(mealByBr)

            meals ::: getMeals(Option(elem.nextElementSibling))
          }
        }
        getMeals(next)
      }

    maybeLunches.getOrElse(Nil)
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
        weekTitleElem     <- doc.select(s"h2:contains(v.$weekNum)").firstOpt orElse doc.select(s"em:contains(V.$weekNum)").firstOpt
        weekdaysContainer <- weekTitleElem.parent.parent.select(s"p:contains(${weekdays.head})").firstOpt.map(_.parent)
        weekDayElem       <- weekdaysContainer.select(s"p:contains($weekday)").firstOpt
        mealsForDay       <- Option(weekDayElem.nextElementSiblings(3).map(_.text).filterNot(_ == "**"))
      } yield {
        mealsForDay.map(Meal.apply)
      }

    maybeMeals.getOrElse(Nil)
  }

  override protected def isMealResultUnreasonable(meals: Seq[Meal]): Boolean = {
    super.isMealResultUnreasonable(meals) || meals.length > 2
  }
}
