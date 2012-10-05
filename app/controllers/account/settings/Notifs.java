package controllers.account.settings;

import models.User;
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
        
        
        
        user.setNotifOnMyTalk(checkboxPresent("notifOnMyTalk"));
        user.setNotifAdminOnAllTalk(checkboxPresent("notifAdminOnAllTalk"));
        user.setNotifAdminOnTalkWithComment(checkboxPresent("notifAdminOnTalkWithComment"));
        user.save();
    	
    	return redirect(routes.Notifs.index());
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
        	notifsForm.notifOnMyTalk = user.hasNotifOnMyTalk();
        	notifsForm.notifAdminOnAllTalk = user.hasNotifAdminOnAllTalk();
        	notifsForm.notifAdminOnTalkWithComment = user.hasNotifAdminOnTalkWithComment();
        	return notifsForm;
        }
    }
}
