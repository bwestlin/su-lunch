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
import play.api.test.WithApplication
import scala.xml._

@RunWith(classOf[JUnitRunner])
class LunchInfoFetcherSpec extends Specification {

  "LunchInfoFetcher" should {

    "Pair all configured restaurants with a parser" in new WithApplication {
      val restaurantWithParser = LunchInfoFetcher().allRestaurantsWithParser

      restaurantWithParser foreach { case (_, parser) =>
          parser must not beNull
      }
      restaurantWithParser.length mustEqual 5
    }
  }
}
