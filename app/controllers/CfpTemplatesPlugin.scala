package controllers

import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.TemplatesPlugin
import securesocial.core.{Identity, IdentityProvider, SecuredRequest}
import play.api.mvc.{RequestHeader, Request}
import play.api.templates.Html
import play.api.templates.Txt
import play.api.Logger
import play.api.data.Form

class CfpTemplatesPlugin(application: play.Application) extends TemplatesPlugin
{


  override def onStart() {
    Logger.info("[securesocial - cfp] loaded templates plugin: %s".format(getClass.getName))
  }

 /**
   * Returns the html for the login page
   * @param request
   * @tparam A
   * @return
   */
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html =
  {
    Logger.info("[securesocial - cfp] getLoginPage")
    views.html.securesocialpages.login(form, msg)
  }

  /**
   * Returns the html for the signup page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    Logger.info("[securesocial - cfp] getSignUpPage")
    views.html.securesocialpages.signUp(form, token)
  }

  /**
   * Returns the html for the start signup page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    Logger.info("[securesocial - cfp] getStartSignUpPage")
    views.html.securesocialpages.startSignUp(form)
  }

  /**
   * Returns the html for the reset password page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    Logger.info("[securesocial - cfp] getStartResetPasswordPage")
    views.html.securesocialpages.startResetPassword(form)
  }

  /**
   * Returns the html for the start reset page
   *
   * @param request
   * @tparam A
   * @return
   */
  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    Logger.info("[securesocial - cfp] getResetPasswordPage")
    views.html.securesocialpages.resetPassword(form, token)
  }

   /**
   * Returns the html for the change password page
   * 
   * Template non customisé car ne doit pas servir pour le CFP
   *
   * @param request
   * @param form
   * @tparam A
   * @return
   */
  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = {
    Logger.info("[securesocial - cfp] getPasswordChangePage")
    securesocial.views.html.passwordChange(form)
  }



  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    Logger.info("[securesocial - cfp] getNotAuthorizedPage")
    views.html.securesocialpages.notAuthorized()
  }


  /**
   * Returns the email sent when a user starts the sign up process
   *
   * @param token the token used to identify the request
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    Logger.info("[securesocial - cfp] getSignUpEmail")
    var signUpUrl = securesocial.core.providers.utils.RoutesHelper.signUp(token).absoluteURL(IdentityProvider.sslEnabled)
    (None, Some(views.html.securesocialmails.signUpEmail(signUpUrl)))
  }

  /**
   * Returns the email sent when the user is already registered
   *
   * @param user the user
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    Logger.info("[securesocial - cfp] getAlreadyRegisteredEmail")
    var resetUrl = securesocial.core.providers.utils.RoutesHelper.startResetPassword().absoluteURL(IdentityProvider.sslEnabled)
    (None, Some(views.html.securesocialmails.alreadyRegisteredEmail(resetUrl, user.fullName)))
  }

  /**
   * Returns the welcome email sent when the user finished the sign up process
   *
   * @param user the user
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    Logger.info("[securesocial - cfp] getWelcomeEmail")
    var loginUrl = securesocial.core.providers.utils.RoutesHelper.login.absoluteURL(IdentityProvider.sslEnabled)
    (None, Some(views.html.securesocialmails.welcomeEmail(loginUrl, user.fullName)))
  }

  /**
   * Returns the email sent when a user tries to reset the password but there is no account for
   * that email address in the system
   *
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    Logger.info("[securesocial - cfp] getUnknownEmailNotice")
    (None, Some(views.html.securesocialmails.unknownEmailNotice(request)))
  }

  /**
   * Returns the email sent to the user to reset the password
   *
   * @param user the user
   * @param token the token used to identify the request
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    Logger.info("[securesocial - cfp] getSendPasswordResetEmail")
    var resetUrl = securesocial.core.providers.utils.RoutesHelper.resetPassword(token).absoluteURL(IdentityProvider.sslEnabled)
    (None, Some(views.html.securesocialmails.passwordResetEmail(resetUrl, user.fullName)))
  }

  /**
   * Returns the email sent as a confirmation of a password change
   * 
   * Template non customisé car ne doit pas servir pour le CFP
   *
   * @param user the user
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    Logger.info("[securesocial - cfp] getPasswordChangedNoticeEmail")
    (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
  }
}