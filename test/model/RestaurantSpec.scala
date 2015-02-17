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

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test.WithApplication

@RunWith(classOf[JUnitRunner])
class RestaurantSpec extends Specification {

  "Restaurant" should {

    "Provide a list of all restaurants" in new WithApplication {
      val restaurants = Restaurant.getAll

      restaurants.length mustEqual 4
      restaurants.map(_.id).toSet mustEqual (1 to 5).filterNot(_ == 3).toSet
      restaurants.forall(_.name.nonEmpty) must beTrue
      restaurants.forall(_.url.nonEmpty) must beTrue
      restaurants.forall(_.parser.nonEmpty) must beTrue
      restaurants.map(_.parser) mustEqual Seq("Lantis", "Fossilen", "Kraftan", "Biofood")
    }
  }
}
