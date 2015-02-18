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

package common

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import common.StringExtensions._

@RunWith(classOf[JUnitRunner])
class StringExtensionsSpec extends Specification {

  "StringExtensions" should {

    "Parse string to option of int" in {
      "1".toIntOpt should beSome(1)
      "0".toIntOpt should beSome(0)
      "-1".toIntOpt should beSome(-1)
      "?".toIntOpt should beNone
    }

    "Trim whitespace including NBSP" in {
      new String(Array[Char](0xA0, 0x20)).trimWhitespace shouldEqual ""
    }
  }
}
