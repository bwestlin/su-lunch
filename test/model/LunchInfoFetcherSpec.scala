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
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.ws.WSResponse
import play.api.test.{PlaySpecification, WithApplication}
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class LunchInfoFetcherSpec extends PlaySpecification with Mockito {

  "LunchInfoFetcher" should {

    "Pair all configured restaurants with a parser" in new WithApplication {
      val restaurantWithParser = LunchInfoFetcher().allRestaurantsWithParser

      restaurantWithParser foreach { case (_, parser) =>
          parser must not beNull
      }
      restaurantWithParser.length mustEqual 4
    }

    "Fetch and parse information about todays lunches" in new WithApplication {
      import LunchInfoParser._

      val lunchInfoFetcher = new LunchInfoFetcher {
        override def fetchUrl(url: String, requestHeaders: Option[Map[String, String]], timeoutSec: Int): Future[WSResponse] = {
          val html = url match {
            case "http://fossilen" => FossilenFixtures.html().toString()
            case "http://kraftan" => KraftanFixtures.html().toString()
          }
          val wsResp = mock[WSResponse]
          wsResp.body returns html
          Future.successful(wsResp)
        }
      }

      val restaurantWithParser = Seq(
        (Restaurant(1, "Fossilen", "http://fossilen", None, "Fossilen"), getParser("Fossilen").get),
        (Restaurant(2, "Kraftan", "http://kraftan", None, "Kraftan"), getParser("Kraftan").get)
      )
      val futureLunchInfo = lunchInfoFetcher.fetchTodaysLunchInfo(DateTime.parse("2015-02-09T12.00"), restaurantWithParser)
      val lunchInfo = await(futureLunchInfo)

      lunchInfo.length mustEqual 2
      val (_, lunchInfo1) = lunchInfo(0)
      lunchInfo1 must beSuccessfulTry
      lunchInfo1.get.length mustEqual 4
      lunchInfo1.get.map(_.description).toSet mustEqual (1 to 4).map("mon-meal" + _).toSet
      val (_, lunchInfo2) = lunchInfo(1)
      lunchInfo2 must beSuccessfulTry
      lunchInfo2.get must beEmpty
    }

    "Handle parsing failures using Try's" in new WithApplication {
      import LunchInfoParser._

      val lunchInfoFetcher = new LunchInfoFetcher {
        override def fetchUrl(url: String, requestHeaders: Option[Map[String, String]], timeoutSec: Int): Future[WSResponse] = {
          val wsResp = mock[WSResponse]
          wsResp.body returns BiofoodFixtures.html(BiofoodFixtures.defaultMealNames(11)).toString()
          Future.successful(wsResp)
        }
      }

      val restaurantWithParser = Seq(
        (Restaurant(1, "Biofood", "http://biofood", None, "Biofood"), getParser("Biofood").get)
      )
      val futureLunchInfo = lunchInfoFetcher.fetchTodaysLunchInfo(DateTime.parse("2014-11-10T12.00"), restaurantWithParser)
      val lunchInfo = await(futureLunchInfo)

      lunchInfo.length mustEqual 1
      val (_, lunchInfo1) = lunchInfo(0)
      lunchInfo1 must beFailedTry
    }

    "Handle fetch failures using Try's" in new WithApplication {
      import LunchInfoParser._

      val lunchInfoFetcher = new LunchInfoFetcher {
        override def fetchUrl(url: String, requestHeaders: Option[Map[String, String]], timeoutSec: Int): Future[WSResponse] = {
          Future.failed(new Exception("Something went wrong"))
        }
      }

      val restaurantWithParser = Seq(
        (Restaurant(1, "Biofood", "http://biofood", None, "Biofood"), getParser("Biofood").get)
      )
      val futureLunchInfo = lunchInfoFetcher.fetchTodaysLunchInfo(DateTime.parse("2014-11-10T12.00"), restaurantWithParser)
      val lunchInfo = await(futureLunchInfo)

      lunchInfo.length mustEqual 1
      val (_, lunchInfo1) = lunchInfo(0)
      lunchInfo1 must beFailedTry.withThrowable[Exception]("Something went wrong")
    }
  }
}
