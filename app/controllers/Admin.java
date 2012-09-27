package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Login and Logout.
 * User: yesnault
 */

@Security.Authenticated(Secured.class)
public class Admin extends Controller {



	public static Result users() {
		return ok(views.html.admin.users.render(User.findAll(), User.findByEmail(request().username())));
	}

}