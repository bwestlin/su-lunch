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

@RunWith(classOf[JUnitRunner])
class LunchInfoParserSpec extends Specification {

  "LunchInfoParser" should {

    "Build a map with all LunchInfoParsers mapped to their corresponding name" in {
      val allParsers = LunchInfoParser.allParsers

      allParsers.size must beEqualTo(4)

      allParsers.get("Lantis") must beSome
      allParsers.get("Fossilen") must beSome
      allParsers.get("Kraftan") must beSome
      allParsers.get("Biofood") must beSome
    }
  }

}
