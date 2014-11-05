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

    if (meals != null && mealResultValidators.exists(_(meals)))
      throw new Exception("Inhämtningen gav ett otillförlitligt resultat")

    meals
  }

  def parse(day: DateTime, body: String): Seq[Meal]

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
  def weekdaysShort = List("Mån", "Tis", "Ons", "Tor", "Fre", "Lör", "Sön")

  def months = List("Januari", "Februari", "Mars", "April", "Maj", "Juni", "Juli", "Augusti", "September", "Oktober", "November", "December")
}

/**
 * Function to parse all lunches for a given day from restaurant Lantis, see http://www.hors.se/restaurang-lantis
 */
object LantisLunchInfoParser extends LunchInfoParser {

  override def parse(dayDT: DateTime, body: String): Seq[Meal] = {
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

  override def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val weekStartDate = dayDT.withDayOfWeek(1)
    val currentWeek = weekStartDate.weekOfWeekyear().get()

    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

    val baseElement = doc.select(".sv-text-portlet-content")

    // Check that the webpage consist of the right week according to today
    val correctWeek = Option(baseElement.select("h2:containsOwn(Meny)").first).map { headerElem =>
      headerElem.text.split(Array(',', '-', ' ')).map(_.trim).toList match {
        case _ :: _ :: week :: _ if week.toInt == currentWeek => true
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

  override def parse(dayDT: DateTime, body: String): Seq[Meal] = {
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

  override def parse(dayDT: DateTime, body: String): Seq[Meal] = {
    val doc = Jsoup.parse(body)

    val weekday = weekdays(dayDT.dayOfWeek.get - 1)

    val dayP = doc.select("#about p:contains(" + weekday + ")").first
    if (dayP != null) {

      def streamLines(p: Element): Stream[String] = {
        def streamByBr(byBr: List[String]): Stream[String] = byBr match {
          case Nil => Stream.empty
          case x :: xs => x #:: streamByBr(xs)
        }

        val html = if (p != null) p.html else null
        if (html == null) Stream.empty
        else {
          val byBr = html.split("<br />").toList.map { (line) => Jsoup.parse(line.trim).text() }
          streamByBr(byBr) #::: streamLines(p.nextElementSibling())
        }
      }

      def getLunches(lines: Stream[String]): List[Meal] = {
        if (lines.isEmpty) List()
        else lines match {
          case x #:: xs if x == weekday || x == "**" => getLunches(xs)
          case x #:: xs if x == null || x.length <= 1 || weekdays.contains(x) => List()
          case x #:: xs => Meal(x) :: getLunches(xs)
        }
      }

      getLunches(streamLines(dayP))
    }
    else null
  }
}
