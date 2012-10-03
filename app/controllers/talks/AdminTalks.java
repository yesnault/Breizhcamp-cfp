package controllers.talks;

import java.util.List;

import models.Comment;
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
		// Fetch all speakers of talks
		for (Talk talk : talks) {
			talk.speaker.fullname.toString();
		}
		
		return ok(list.render(user, talks));
	}
	
	public static Result seeTalk(Long idTalk) {
		User user = User.findByEmail(request().username());
		if (!user.admin) {
			return GO_INDEX;
		}
		
		Talk talk = Talk.find.byId(idTalk);

		// FIXME (find a better solution...) 
		// Fetch speaker of talk.
		talk.speaker.fullname.toString();
		
		return ok(seeTalk.render(user, talk));
	}
	
	public static Result saveComment(Long idTalk) {
		User user = User.findByEmail(request().username());
		if (!user.admin) {
			return GO_INDEX;
		}
		Talk talk = Talk.find.byId(idTalk);
		String commentForm = request().body().asFormUrlEncoded().get("comment")[0];
		
		if (commentForm.length() != 0) {
			Comment comment = new Comment();
			comment.author = user;
			comment.comment = commentForm;
			comment.talk = talk;
			comment.save();
		}
		return redirect(controllers.talks.routes.AdminTalks.seeTalk(idTalk));
	}

}
