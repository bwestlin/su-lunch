package impl.lunchInfo

import collection.JavaConversions._

import org.jsoup._
import nodes.Element
import org.joda.time.{DateTime, Period}

object LunchInfoFetcher {

  def fetchTodaysLunchInfo: Seq[(Restaurant, Any)] = {

    type RestaurantFetcher = (String) => List[Meal]

    var todayDT: DateTime = new DateTime()
    //todayDT = todayDT.minus(Period.days(1))

    val restaurantsToFetch = List(
      (
        "Restaurang Lantis",
        "http://www.hors.se/restaurang-lantis",
        (url: String) => {
          try {
            val doc = Jsoup.connect(url).get();

            val lunchmenulist = doc.select(".lunchmenulist").first()

            val weekdays = List("måndag", "tisdag", "onsdag", "torsdag", "fredag", "lördag", "söndag")
            val weekday = weekdays(todayDT.dayOfWeek().get() - 1)
            if (lunchmenulist.select("h2:containsOwn(" + weekday + ")") != null) {

              val typMap = Map(
                ("kott" -> "Kött"),
                ("fisk" -> "Fisk"),
                ("veg" -> "Vegetarisk")
              )

              lunchmenulist.select(".lunchmenulisttext").map(elem => {
                val typ = elem.parent().className()
                Meal(typMap(typ) + ": " + elem.text())
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
        (url: String) => {
          try {
            val doc = Jsoup.connect(url).get();

            val weekdays = List("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag")
            val weekday = weekdays(todayDT.dayOfWeek().get() - 1)

            val dayTd = doc.select("td:containsOwn(" + weekday + ")").first()
            val next = dayTd.parent().nextElementSibling().nextElementSibling()

            def getRows(row: Element, xs: List[Meal]): List[Meal] = {
              val tds = if (row != null) row.select("td") else null
              val firstTd = if (tds != null) tds.first() else null

              if (firstTd == null || firstTd.className() == "rubriksmaller") xs
              else {
                Meal(row.text()) :: getRows(row.nextElementSibling(), xs)
              }
            }

            getRows(next, List())
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
        (url: String) => {
          try {
            val doc = Jsoup.connect(url).get();

            val weekdays = List("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag")
            val weekday = weekdays(todayDT.dayOfWeek().get() - 1)

            val dayP = doc.select("#about p:contains(" + weekday + ")").first()


            def getLunches(p: Element, xs: List[Meal]): List[Meal] = {
              val text = if (p != null) p.text().trim else null
              if (text == null || text.length <= 1) xs
              else if (text == "**")
                getLunches(p.nextElementSibling(), xs)
              else {
                Meal(text) :: getLunches(p.nextElementSibling(), xs)
              }
            }

            getLunches(dayP.nextElementSibling(), List())
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

    restaurantsToFetch.map(t => {
      val (name, url, fetcher) = t
      val meals = fetcher(url)
      ( Restaurant(name, url), meals )
    }).filter(elem => elem != null)
  }

}
