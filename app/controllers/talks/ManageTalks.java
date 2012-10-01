package controllers.talks;

import java.util.List;

import models.Talk;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import controllers.Secured;
import views.html.talks.managetalks.index;

@Security.Authenticated(Secured.class)
public class ManageTalks extends Controller {
	
	public static Result index() {
		User user = User.findByEmail(request().username());
		List<Talk> talks = Talk.findBySpeaker(user);
		
		return ok(index.render(user, talks));
	}

}
