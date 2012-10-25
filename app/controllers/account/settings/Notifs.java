package controllers.account.settings;

import models.User;
import org.codehaus.jackson.JsonNode;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import controllers.Secured;
import views.html.account.settings.notifs;

@Security.Authenticated(Secured.class)
public class Notifs extends Controller {

    public static Result index() {
        User user = User.findByEmail(request().username());
        
        Form<NotifsForm> notifsForm = form(NotifsForm.class).fill(NotifsForm.fromUser(user));
    	return ok(notifs.render(user, notifsForm));
    }
    
    public static Result save() {

        User user = User.findByEmail(request().username());

        JsonNode userJson = request().body().asJson();
        boolean isJson;
        boolean  notifOnMyTalk;
        boolean  notifAdminOnAllTalk;
        boolean  notifAdminOnTalkWithComment;
        if (userJson == null) {
            isJson = false;
            notifOnMyTalk = checkboxPresent("notifOnMyTalk");
            notifAdminOnAllTalk = checkboxPresent("notifAdminOnAllTalk");
            notifAdminOnTalkWithComment = checkboxPresent("notifAdminOnTalkWithComment");
        } else {
            isJson = true;
            notifOnMyTalk = userJson.get("notifOnMyTalk").asBoolean();
            notifAdminOnAllTalk = userJson.get("notifAdminOnAllTalk").asBoolean();
            notifAdminOnTalkWithComment = userJson.get("notifAdminOnTalkWithComment").asBoolean();
        }


        user.setNotifOnMyTalk(notifOnMyTalk);
        user.setNotifAdminOnAllTalk(notifAdminOnAllTalk);
        user.setNotifAdminOnTalkWithComment(notifAdminOnTalkWithComment);
        user.save();
    	if (isJson) {
            return ok();
        } else {
    	    return redirect(routes.Notifs.index());
        }
    }
    
    private static boolean checkboxPresent(String name) {
    	return request().body().asFormUrlEncoded().get(name) != null
    			&& request().body().asFormUrlEncoded().get(name).length > 0;
    }
    
    public static class NotifsForm {

        @Constraints.Required
    	public Boolean notifOnMyTalk;
        
        @Constraints.Required
    	public Boolean notifAdminOnAllTalk;
        
        @Constraints.Required
    	public Boolean notifAdminOnTalkWithComment;
        
        public static NotifsForm fromUser(User user) {
        	NotifsForm notifsForm = new NotifsForm();
        	notifsForm.notifOnMyTalk = user.getNotifOnMyTalk();
        	notifsForm.notifAdminOnAllTalk = user.getNotifAdminOnAllTalk();
        	notifsForm.notifAdminOnTalkWithComment = user.getNotifAdminOnTalkWithComment();
        	return notifsForm;
        }
    }
}
