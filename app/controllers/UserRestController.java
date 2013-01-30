package controllers;


import static play.libs.Json.toJson;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

@SecureSocial.SecuredAction(ajaxCall=true)
public class UserRestController extends Controller {

    public static Result get() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.findByExternalId(socialUser.id().id(), socialUser.id().providerId());
        if (!user.admin) {
            return unauthorized();
        }
        return ok(toJson(User.findAll()));
    }

    public static Result getUser(Long id) {
           User user = User.findById(id);
          if (user == null) {
              return badRequest();
          }

          return ok(toJson(user));
      }

    public static Result getUserLogged() {
    	Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
    	
    	Logger.debug("getUserLogged : " + socialUser.id().id() + "/" + socialUser.id().providerId());
    	User user = User.findByExternalId(socialUser.id().id(), socialUser.id().providerId());
    	
    	return ok(toJson(user));
    }
}
