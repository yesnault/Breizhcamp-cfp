package controllers.talks;

import models.Comment;
import models.Talk;
import models.User;
import org.codehaus.jackson.JsonNode;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.talks.seetalks.seeTalk;
import controllers.Secured;
import controllers.routes;

import java.util.Iterator;
import java.util.Map;


@Security.Authenticated(Secured.class)
public class SeeTalks extends Controller {
	
	private static final Result GO_INDEX = redirect(routes.Application.index());
	
	public static Result seeTalk(Long idTalk) {
		User user = User.findByEmail(request().username());
		Talk talk = Talk.find.byId(idTalk);
		if (!user.admin && user.id != talk.speaker.id) {
			return GO_INDEX;
		}
		
		// FIXME (find a better solution...) 
		// Fetch speaker of talk.
		talk.speaker.fullname.toString();
		
		return ok(seeTalk.render(user, talk));
	}
	
	public static Result saveComment(Long idTalk) {
		User user = User.findByEmail(request().username());
		Talk talk = Talk.find.byId(idTalk);

        boolean isJson = true;
        String commentForm = null;
        JsonNode node = request().body().asJson();
        if (node == null) {
            isJson = false;
            commentForm = request().body().asFormUrlEncoded().get("comment")[0];
        } else {
            commentForm = node.get("comment").asText();
        }


        if (!user.admin && !user.id.equals(talk.speaker.id)) {
            if (isJson) {
                return badRequest();
            } else {
                return GO_INDEX;
            }
        }
		
		if (commentForm.length() > 0 && commentForm.length() <= 140) {
			Comment comment = new Comment();
			comment.author = user;
			comment.comment = commentForm;
			comment.talk = talk;
			comment.save();
			comment.sendMail();
		}
        if (isJson) {
            return ok();
        } else {
            return redirect(controllers.talks.routes.SeeTalks.seeTalk(idTalk));
        }
	}

}
