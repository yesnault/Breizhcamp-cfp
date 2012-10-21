package controllers.talks;

import static play.libs.Json.toJson;

import java.util.List;

import models.Talk;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import controllers.Secured;

@Security.Authenticated(Secured.class)
public class TalkRestController extends Controller {
	
	public static Result getById(Long idTalk) {
		Talk talk = Talk.find.byId(idTalk); 
		return ok(toJson(talk));
	}
	
	public static Result get() {
		User user = User.findByEmail(request().username());
		List<Talk> talks = Talk.findBySpeaker(user);	
		return ok(toJson(talks));
	}
	
	
	public static Result save() {
		User user = User.findByEmail(request().username());
		Form<Talk> talkForm = form(Talk.class).bindFromRequest();
		if (talkForm.hasErrors()) {
			return badRequest();
		}
		
		Talk formTalk = talkForm.get();
		
		if (formTalk.id==null) {
			// Nouveau talk
			formTalk.speaker = user;
			if (Talk.findByTitle(formTalk.title) != null) {
				return badRequest("error.talk.already.exist");
			}
			formTalk.save();
		} else {
			// Mise à jour d'un talk
			Talk dbTalk = Talk.find.byId(formTalk.id);
			if (!formTalk.title.equals(dbTalk.title) 
					&& Talk.findByTitle(formTalk.title) != null) {
				return badRequest("error.talk.already.exist");
			}
			dbTalk.title = formTalk.title;
			dbTalk.description = formTalk.description;
			dbTalk.save();
		}		

		// HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
	}
	
	
	
	public static Result delete(Long idTalk) {
		Talk talk = Talk.find.byId(idTalk);
		talk.delete();
		// HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
	}

}
