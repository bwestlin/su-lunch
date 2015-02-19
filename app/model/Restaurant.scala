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

import com.typesafe.config.ConfigRenderOptions
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import play.api.Play

case class Restaurant(id: Int,
                      name: String,
                      url: String,
                      requestHeaders: Option[Map[String, String]],
                      parser: String)

object Restaurant {

  implicit val restaurantReads = Json.reads[Restaurant]

  def getAll: Seq[Restaurant] = {

    for {
      restaurantConfig <- Play.current.configuration.underlying.getList("restaurants").asScala.toSeq
    } yield {
      Json.parse(restaurantConfig.render(ConfigRenderOptions.concise())).as[Restaurant]
    }
  }
}
