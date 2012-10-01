package controllers.account.settings;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.account.settings.account;
import controllers.Secured;

@Security.Authenticated(Secured.class)
public class Account extends Controller {

    public static Result index() {
        User user = User.findByEmail(request().username());
        
        return ok(account.render(user));
    }
}
