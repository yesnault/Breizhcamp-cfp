package controllers.talks;

import java.util.List;

import models.Talk;
import models.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import controllers.Secured;
import views.html.talks.managetalks.*;
import views.html.talks.submittalk.index;

import static play.libs.Json.toJson;

@Security.Authenticated(Secured.class)
public class TalkRestController extends Controller {
	
	public static Result get() {
		User user = User.findByEmail(request().username());
		List<Talk> talks = Talk.findBySpeaker(user);
		
		return ok(toJson(talks));
	}
	
	public static Result save() {
		User user = User.findByEmail(request().username());
		Form<Talk> talkForm = form(Talk.class).bindFromRequest();
		if (talkForm.hasErrors()) {
			return badRequest(index.render(user, talkForm));
		}
		
		Talk talk = talkForm.get();
		talk.speaker = user;
		
		if (Talk.findByTitle(talk.title) != null) {
			flash("error", Messages.get("error.talk.already.exist"));
			return badRequest(index.render(user, talkForm));
		}
		
		talk.save();

		// HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
	}
	

}
