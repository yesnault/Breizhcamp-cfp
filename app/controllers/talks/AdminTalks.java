package controllers.talks;

import java.util.List;

import models.Talk;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import controllers.Secured;
import controllers.routes;
import views.html.talks.admintalks.*;


@Security.Authenticated(Secured.class)
public class AdminTalks extends Controller {
	
	private static final Result GO_INDEX = redirect(routes.Application.index());
	
	public static Result list() {
		User user = User.findByEmail(request().username());
		if (!user.admin) {
			return GO_INDEX;
		}
		
		List<Talk> talks = Talk.find.all(); 
		
		// FIXME (find a better solution...) 
		// Fetch all user of talks
		for (Talk talk : talks) {
			talk.speaker.fullname.toString();
		}
		
		return ok(list.render(user, talks));
	}

}
