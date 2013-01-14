package controllers;


import static play.libs.Json.toJson;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

public class UserRestController extends Controller {

	@SecureSocial.SecuredAction
    public static Result get() {
        User user = User.findByEmail(request().username());
        if (!user.admin) {
            return unauthorized();
        }
        return ok(toJson(User.findAll()));
    }

    @SecureSocial.SecuredAction
    public static Result getUser(Long id) {
           User user = User.findById(id);
          if (user == null) {
              return badRequest();
          }

          return ok(toJson(user));
      }

    @SecureSocial.UserAwareAction
    public static Result getUserLogged() {
    	Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
    	if (socialUser == null) return unauthorized();
//    	User userCfp = new User();
//    	userCfp.email = user.email().get();
//    	userCfp.fullname = user.fullName();
    	 //  user.avatarUrl().get();
    	Logger.debug("getUserLogged : " + socialUser.email().get());
    	User user = User.findByEmail(socialUser.email().get());
    	
    	return ok(toJson(user));
    }
}
