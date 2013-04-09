package model

import collection.JavaConversions._

import org.jsoup._
import nodes.Element
import org.joda.time.{DateTime, Period}

object LunchInfoFetcher {

  def fetchTodaysLunchInfo: Seq[(Restaurant, Any)] = {

    type RestaurantFetcher = (String) => Any

    var todayDT: DateTime = new DateTime()
    //todayDT = todayDT.minus(Period.days(1))

    val restaurantsToFetch: List[(String, String, RestaurantFetcher)] = List(
      (
        "Restaurang Lantis",
        "http://www.hors.se/restaurang-lantis",
        url => {
          try {
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
          catch {
            case e: Exception => {
              e.printStackTrace()
              e
            }
          }
        }
      ),
      (
        "Stora Skuggans Wärdshus",
        "http://gastrogate.com/restaurang/storaskuggan/page/3",
        url => {
          try {
            val doc = Jsoup.connect(url).timeout(10*1000).ignoreHttpErrors(true).get()

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
          catch {
            case e: Exception => {
              e.printStackTrace()
              e
            }
          }
        }
      ),
      (
        "Värdshuset Kräftan",
        "http://www.kraftan.nu/",
        url => {
          try {
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
          catch {
            case e: Exception => {
              e.printStackTrace()
              e
            }
          }
        }
      )
    )

    // Fetch lunches from each restaurant in parallel
    restaurantsToFetch.par.map({
      case (name, url, fetcher) => {
        (Restaurant(name, url), fetcher(url))
      }
    }).seq
  }

}
