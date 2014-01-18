package controllers.account.settings;

import controllers.BaseController;
import models.User;

import com.fasterxml.jackson.databind.JsonNode;

import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

@SecureSocial.SecuredAction(ajaxCall=true)
public class Notifs extends BaseController {

    public static Result save() {

        User user = getLoggedUser();

        JsonNode userJson = request().body().asJson();
        user.setNotifOnMyProposal(userJson.get("notifOnMyProposal").asBoolean());
        user.setNotifAdminOnAllProposal(userJson.get("notifAdminOnAllProposal").asBoolean());
        user.setNotifAdminOnProposalWithComment(userJson.get("notifAdminOnProposalWithComment").asBoolean());
        user.save();
        return ok();
    }
}
