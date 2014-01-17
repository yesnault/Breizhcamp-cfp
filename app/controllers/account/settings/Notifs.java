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
        user.setNotifOnMyTalk(userJson.get("notifOnMyTalk").asBoolean());
        user.setNotifAdminOnAllTalk(userJson.get("notifAdminOnAllTalk").asBoolean());
        user.setNotifAdminOnTalkWithComment(userJson.get("notifAdminOnTalkWithComment").asBoolean());
        user.save();
        return ok();
    }
}
