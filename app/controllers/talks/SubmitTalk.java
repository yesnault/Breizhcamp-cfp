package controllers.talks;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.talks.submittalk.index;
import controllers.Secured;

@Security.Authenticated(Secured.class)
public class SubmitTalk extends Controller {
	
	public static Result index() {
		
		return ok(index.render(User.findByEmail(request().username())));
	}

}
