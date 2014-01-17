package controllers;

import models.User;
import play.mvc.Controller;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BaseController extends Controller {

    protected static User getLoggedUser() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        checkProfileCompletion(socialUser);
        User user = User.findByEmail(socialUser.email().get());
        return user;
    }

    private static void checkProfileCompletion(Identity socialUser) {
        if (!socialUser.email().isDefined()) {
            forbidden();
        }
        // TODO
    }
}
