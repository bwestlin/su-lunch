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

import play.api._
import play.api.mvc.{Handler, RequestHeader}
import play.api.http.HeaderNames

object Global extends GlobalSettings with HeaderNames {

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {

    // Do ip-address logging for some controllers
    if (Seq("/assets", "/webjars").forall { !request.path.startsWith(_) }) {
      val ipAddress = request.headers.get(X_FORWARDED_FOR) match {
        case Some(ip: String) => ip
        case None => request.remoteAddress
      }
      Logger.info(request.path + " requested from ip-address: " + ipAddress + ", remoteAddress is: " + request.remoteAddress)
    }

    super.onRouteRequest(request)
  }
}
