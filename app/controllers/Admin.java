package controllers;

import java.util.List;
import java.util.Map;

import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Login and Logout.
 * User: yesnault
 */

@Security.Authenticated(Secured.class)
public class Admin extends Controller {

	private static final Result GO_INDEX = redirect(routes.Application.index());

	public static Result users() {
		User user = User.findByEmail(request().username());
		if (!user.admin) {
			return GO_INDEX;
		}
		return ok(views.html.admin.users.render(User.findAll(), user));
	}

    public static Result varenv() {
        Map<String, String> variables = System.getenv();
        Logger.info("Variable environement");
        for (Map.Entry<String, String> entry : variables.entrySet())
        {
            String name = entry.getKey();
            String value = entry.getValue();
            Logger.info(name + "=" + value);
        }

        return ok();
    }
	
	public static Result submitUsers() {
		
		String[] emails = request().body().asFormUrlEncoded().get("email");
		
		for (String oneEmail : emails) {
			String[] admin = request().body().asFormUrlEncoded().get(oneEmail);
			User user = User.findByEmail(oneEmail);
			if (admin != null && admin.length == 1) {
				if (user != null && !user.admin) {
					user.admin = true;
					user.save();
				}
			} else {
				if (user != null && user.admin) {
					user.admin = false;
					user.save();
				}
			}
		}
		
		
		return redirect(routes.Admin.users());
	}
	
	public static class AdminUsers {
		public List<User> users;
	}

}