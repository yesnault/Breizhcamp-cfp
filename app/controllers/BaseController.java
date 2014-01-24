package controllers;

import models.Event;
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
        User user = User.findByEmail(socialUser.email().get());
        return user;
    }


    protected static Event getEvent() {
        Event event = Event.findByUrl(request().host());
        if (event == null) {
            event = Event.getDefaut(request().host());
        }
        return event;
    }
}

