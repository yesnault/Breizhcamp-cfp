package service

import securesocial.core._
import play.api.libs.oauth.{RequestToken, OAuthCalculator}
import play.api.libs.ws.WS
import play.api.{Application, Logger}
import CustomLinkedInOAuth2Provider._


/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
class CustomLinkedInOAuth2Provider(application: Application) extends OAuth2Provider(application) {

  override def id = CustomLinkedInOAuth2Provider.LinkedIn

  override def fillProfile(user: SocialUser): SocialUser = {
    val accessToken = user.oAuth2Info.get.accessToken
    val promise = WS.url(CustomLinkedInOAuth2Provider.Api + accessToken).get()

    try {
      val response = awaitResult(promise)
      val me = response.json
      (me \ ErrorCode).asOpt[Int] match {
        case Some(error) => {
          val message = (me \ Message).asOpt[String]
          val requestId = (me \ RequestId).asOpt[String]
          val timestamp = (me \ Timestamp).asOpt[String]
          Logger.error(
            "Error retrieving information from LinkedIn. Error code: %s, requestId: %s, message: %s, timestamp: %s"
              format(error, message, requestId, timestamp)
          )
          throw new AuthenticationException()
        }
        case _ => {
          val userId = (me \ Id).as[String]
          var email = (me \ Email).asOpt[String]
          val firstName = (me \ FirstName).asOpt[String].getOrElse("")
          val lastName = (me \ LastName).asOpt[String].getOrElse("")
          val fullName = (me \ FormattedName).asOpt[String].getOrElse("")
          val avatarUrl = (me \ PictureUrl).asOpt[String]

          SocialUser(user).copy(
            identityId = IdentityId(userId, id),
            email = email,
            firstName = firstName,
            lastName = lastName,
            fullName= fullName,
            avatarUrl = avatarUrl
          )
        }
      }
    } catch {
      case e: Exception => {
        Logger.error("[securesocial] error retrieving profile information from LinkedIn", e)
        throw new AuthenticationException()
      }
    }
  }
}

object CustomLinkedInOAuth2Provider {
  val Api = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address,formatted-name,picture-url)?format=json&oauth2_access_token="
  val LinkedIn = "linkedin"
  val ErrorCode = "errorCode"
  val Message = "message"
  val RequestId = "requestId"
  val Timestamp = "timestamp"
  val Id = "id"
  val FirstName = "firstName"
  val LastName = "lastName"
  val Email = "emailAddress"
  val FormattedName = "formattedName"
  val PictureUrl = "pictureUrl"
}

