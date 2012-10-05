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
        
        Form<NotifsForm> notifsForm = form(NotifsForm.class);
    	return ok(notifs.render(user, notifsForm));
    }
    
    public static Result save() {
    	return null;
    }
    
    public static class NotifsForm {

        @Constraints.Required
    	public Boolean notifOnMyTalk;
        
        @Constraints.Required
    	public Boolean notifAdminOnAllTalk;
        
        @Constraints.Required
    	public Boolean notifAdminOnTalkWithComment;
    }
}
