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

package controllers

import model.{Meal, Restaurant, LunchInfoFetcher}
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.specs2.specification.Scope
import play.api.http.MimeTypes
import play.api.mvc.Controller
import play.api.test._

import scala.concurrent.Future
import scala.util.Success

@RunWith(classOf[JUnitRunner])
class LunchInfoControllerSpec extends PlaySpecification with Mockito {

  trait SetupMocks extends Scope {

    val mockedLunchInfoFetcher = mock[LunchInfoFetcher]

    val controller = new LunchInfoController with Controller {
      override def lunchInfoFetcher = mockedLunchInfoFetcher
    }

    val restaurant1 = Restaurant(1, "Restaurant1", "http://foo.bar/1", None, "restaurant1")
    val restaurant2 = Restaurant(2, "Restaurant2", "http://foo.bar/2", None, "restaurant2")

    val lunchInfoResult = Seq(
      (restaurant1, Success(Seq(Meal("meal1_1"), Meal("meal1_2")))),
      (restaurant2, Success(Seq(Meal("meal2_1"), Meal("meal2_2"))))
    )

    mockedLunchInfoFetcher.fetchTodaysLunchInfo(any, any) returns Future.successful(lunchInfoResult)
  }

  "LunchInfoController" should {

    def validateNoCacheHeaders(headers: Map[String, String]) = {
      headers(CACHE_CONTROL) mustEqual "no-cache, no-store, must-revalidate"
      headers(PRAGMA) mustEqual "no-cache"
      // TODO Figure out why setting expires header does not work
      //headers(EXPIRES) mustEqual "0"
    }

    "Render index page" in new WithApplication with SetupMocks {
      val result = controller.index().apply(FakeRequest())
      status(result) mustEqual OK
      contentAsString(result) must contain("<div id=\"todaysLunches\"")
    }

    "Render todaysLunches page" in new WithApplication with SetupMocks {
      val result = call(controller.todaysLunches, FakeRequest())
      status(result) mustEqual OK
      validateNoCacheHeaders(headers(result))

      val textToVerify: Seq[String] = lunchInfoResult.map { case (restaurant, Success(meals)) =>
        Seq(restaurant.name, restaurant.url) ++ meals.map(_.description)
      }.flatten

      textToVerify.foreach { text =>
        contentAsString(result) must contain(text)
      }
    }

    "Render todaysLunches json" in new WithApplication with SetupMocks {
      val result = call(controller.todaysLunchesJson, FakeRequest())
      status(result) mustEqual OK
      validateNoCacheHeaders(headers(result))

      val textToVerify: Seq[String] = lunchInfoResult.map { case (restaurant, Success(meals)) =>
        Seq(restaurant.name, restaurant.url) ++ meals.map(_.description)
      }.flatten

      textToVerify.foreach { text =>
        contentAsString(result) must contain(text)
      }
    }

    "Generate Javascript routes" in new WithApplication with SetupMocks {
      val result = call(controller.jsRoutes, FakeRequest())
      status(result) mustEqual OK
      header(CONTENT_TYPE, result) must beSome(MimeTypes.JAVASCRIPT)

      contentAsString(result) must contain("index")
      contentAsString(result) must contain("todaysLunches")
      contentAsString(result) must contain("todaysLunchesJson")
    }
  }
}
