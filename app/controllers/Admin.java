package controllers;

import models.User;
import models.VoteStatus;
import models.VoteStatusEnum;
import org.codehaus.jackson.JsonNode;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Iterator;
import java.util.Map;

import static play.libs.Json.toJson;

/**
 * Login and Logout.
 * User: yesnault
 */

@Security.Authenticated(Secured.class)
public class Admin extends Controller {
	public static Result submitUsers() {
        User userRequest = User.findByEmail(request().username());
        if (!userRequest.admin) {
            return unauthorized();
        }
        JsonNode node = request().body().asJson();
        Iterator<Map.Entry<String, JsonNode>> iteratorMails = node.getFields();
        while (iteratorMails.hasNext()) {
            Map.Entry<String, JsonNode> entry = iteratorMails.next();
            String mail = entry.getKey();
            boolean admin = entry.getValue().asBoolean();
            User user = User.findByEmail(mail);
            if (admin) {
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
        return ok();
	}

    public static class ResultVote {

        public ResultVote(VoteStatusEnum status) {
            this.status = status;
        }

        public VoteStatusEnum status;
    }

    public static Result getVoteStatus() {
        User user = User.findByEmail(request().username());
        if (!user.admin) {
            return unauthorized();
        }
        return ok(toJson(new ResultVote(VoteStatus.getVoteStatus())));
    }

    public static Result changeVoteStatus(String newStatus) {
        User user = User.findByEmail(request().username());
        if (!user.admin) {
            return unauthorized();
        }
        VoteStatus.changeVoteStatus(VoteStatusEnum.valueOf(newStatus));
        return ok();
    }

}