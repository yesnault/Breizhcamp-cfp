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

@Security.Authenticated(Secured.class)
public class ManageTalks extends Controller {
	
	public static Result index() {
		User user = User.findByEmail(request().username());
		List<Talk> talks = Talk.findBySpeaker(user);
		
		return ok(index.render(user, talks));
	}
	
	public static Result edit(Long idTalk) {
		User user = User.findByEmail(request().username());
		
		Talk talk = Talk.find.byId(idTalk);
		
		Form<Talk> talkForm = form(Talk.class).fill(talk);
		
		return ok(edit.render(user, idTalk, talkForm));
	}
	
	public static Result save(Long idTalk) {
		User user = User.findByEmail(request().username());
		Form<Talk> talkForm = form(Talk.class).bindFromRequest();
		if (talkForm.hasErrors()) {
			return badRequest(edit.render(user, idTalk, talkForm));
		}
		
		Talk dbTalk = Talk.find.byId(idTalk);
		Talk formTalk = talkForm.get();
		
		if (!formTalk.title.equals(dbTalk.title) 
				&& Talk.findByTitle(formTalk.title) != null) {
			flash("error", Messages.get("error.talk.already.exist"));
			return badRequest(edit.render(user, idTalk, talkForm));
		}
		
		dbTalk.title = formTalk.title;
		dbTalk.description = formTalk.description;
		dbTalk.save();
		
		
		return index();
	}
	
	public static Result delete(Long idTalk) {
		Talk talk = Talk.find.byId(idTalk);
		
		talk.delete();
		
		return index();
	}

}
