package controllers.talks;


import models.Talk;
import models.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.talks.submittalk.index;
import controllers.Secured;

@Security.Authenticated(Secured.class)
public class SubmitTalk extends Controller {
	
	public static Result index() {
		
		User user = User.findByEmail(request().username());
		
		return ok(index.render(user, form(Talk.class)));
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
		
		return ok(views.html.dashboard.index.render(user));
	}

}
