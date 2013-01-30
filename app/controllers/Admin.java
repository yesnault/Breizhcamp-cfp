package controllers;

import static play.libs.Json.toJson;

import java.util.Iterator;
import java.util.Map;

import models.User;
import models.VoteStatus;
import models.VoteStatusEnum;

import org.codehaus.jackson.JsonNode;

import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;



@SecureSocial.SecuredAction(ajaxCall=true)
public class Admin extends Controller {

    public static User getLoggedUser() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.findByExternalId(socialUser.id().id(), socialUser.id().providerId());
        return user;
    }

	public static Result submitUsers() {
        User userRequest = getLoggedUser();
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
        User user = getLoggedUser();
        if (!user.admin) {
            return unauthorized();
        }
        return ok(toJson(new ResultVote(VoteStatus.getVoteStatus())));
    }

    public static Result changeVoteStatus(String newStatus) {
        User user = getLoggedUser();
        if (!user.admin) {
            return unauthorized();
        }
        VoteStatus.changeVoteStatus(VoteStatusEnum.valueOf(newStatus));
        return ok();
    }

}