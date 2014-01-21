package controllers

import models._
import play.api._
import play.api.mvc._
import securesocial.core.Identity

object Application extends BaseController with securesocial.core.SecureSocial {

  def index = SecuredAction { implicit request =>
      request.user.email match {
          case Some(email) =>
            Ok(views.html.index(User.findByEmail(email)))
              .withHeaders("Cache-Control"->"public, must-revalidate, max-age=84600")

          case _ => Unauthorized("Email not found") // OAuth is supposed to be configured to request email scope
      }

  }

}