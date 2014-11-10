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

import play.api._
import cache.Cached
import mvc._
import play.api.http.MimeTypes
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import model.LunchInfoFetcher

object LunchInfo extends Controller {

  val noCacheHeaders = Seq(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    PRAGMA -> "no-cache",
    EXPIRES -> "0"
  )

  lazy val cacheDuration = Play.maybeApplication.flatMap {
    _.mode match {
      // Cache for 10 minutes in production mode
      case Mode.Prod => Some(60 * 10)
      case _ => None
    }
  }.getOrElse(1)

  def index = Action { implicit request =>
    Ok(views.html.lunchInfo.index())
  }

  def todaysLunches = Cached("todaysLunches", cacheDuration) {
    Action.async { implicit request =>
      LunchInfoFetcher.fetchTodaysLunchInfo().map { todaysLunches =>
        Ok(views.html.lunchInfo.todaysLunches(todaysLunches)).withHeaders(noCacheHeaders: _*)
      }
    }
  }

  val jsReverseRoutes = {
    val jsRoutesClass = classOf[routes.javascript]
    val controllers = jsRoutesClass.getFields.map(_.get(null))
    controllers.flatMap { controller =>
      controller.getClass.getDeclaredMethods.map { action =>
        action.invoke(controller).asInstanceOf[play.core.Router.JavascriptReverseRoute]
      }
    }
  }

  def jsRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(jsReverseRoutes: _*)).as(MimeTypes.JAVASCRIPT)
  }
}