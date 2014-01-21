package controllers

import play.api._
import play.api.mvc._

object Application extends Controller with securesocial.core.SecureSocial {

  def index = SecuredAction {
    Ok(views.html.index("Call for Papers")).withHeaders("Cache-Control"->"public, must-revalidate, max-age=84600")
  }

}