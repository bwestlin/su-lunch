import play.api._
import play.api.mvc.{Handler, RequestHeader}

object Global extends GlobalSettings {

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {

    // Do ip-address logging for some controllers
    if (Seq("/assets", "/webjars").forall { !request.path.startsWith(_) }) {

      import play.api.http.HeaderNames._

      val ipAddress = request.headers.get(X_FORWARDED_FOR) match {
        case Some(ip: String) => ip
        case None => request.remoteAddress
      }
      Logger.info(request.path + " requested from ip-address: " + ipAddress + ", remoteAddress is: " + request.remoteAddress)
    }

    super.onRouteRequest(request)
  }
}
