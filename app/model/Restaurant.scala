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

case class Restaurant(id: Integer, name: String, url: String, fetcher: String) {
}

object Restaurant {

  def getAll: Seq[Restaurant] = {
    Seq(
      Restaurant(1, "Restaurang Lantis", "http://www.hors.se/restaurang-lantis", "Lantis"),
      Restaurant(2, "Restaurang Fossilen", "http://nrm.se/besokmuseet/restaurangfossilen", "Fossilen"),
      Restaurant(3, "Stora Skuggans Wärdshus", "http://gastrogate.com/restaurang/storaskuggan/page/3", "StoraSkuggan"),
      Restaurant(4, "Värdshuset Kräftan", "http://www.kraftan.nu/", "Kraftan")
    )
  }
}
