package controllers.account.settings;

import controllers.Secured;
import models.User;
import org.codehaus.jackson.JsonNode;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class Notifs extends Controller {
    
    public static Result save() {

        User user = User.findByEmail(request().username());

        JsonNode userJson = request().body().asJson();
        user.setNotifOnMyTalk(userJson.get("notifOnMyTalk").asBoolean());
        user.setNotifAdminOnAllTalk(userJson.get("notifAdminOnAllTalk").asBoolean());
        user.setNotifAdminOnTalkWithComment(userJson.get("notifAdminOnTalkWithComment").asBoolean());
        user.save();
        return ok();
    }
}
