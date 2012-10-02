package controllers.account.settings;

import models.Lien;
import models.User;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import play.data.Form;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.account.settings.account;
import controllers.Secured;

@Security.Authenticated(Secured.class)
public class Account extends Controller {

    public static Result index() {
        User user = User.findByEmail(request().username());
        
        Form<AccountForm> accountForm = form(AccountForm.class).fill(AccountForm.fromUser(user));
        
        return ok(account.render(user, accountForm));
    }
    
    public static Result save() {
        User user = User.findByEmail(request().username());
        
        Form<AccountForm> accountForm = form(AccountForm.class).bindFromRequest();
        
        if (accountForm.hasErrors()) {
        	return badRequest(account.render(user, accountForm));
        }
        
        user.description = accountForm.get().description;
        user.save();
        
        return redirect(controllers.account.settings.routes.Account.index());	
    }
    
    public static Result getLiens(Long idUser) {
    	ObjectNode result = Json.newObject();
    	ArrayNode liensJson = result.putArray("liens");
    	for (Lien lien : User.find.byId(idUser).getLiens()) {
    		ObjectNode lienJson = liensJson.addObject();
    		lienJson.put("label", lien.label);
    		lienJson.put("url", lien.url.toString());
    	}
    	return ok(result);
    }
    
    public static class AccountForm {

        @Constraints.Required
        @Formats.NonEmpty
        @Constraints.MaxLength(2000)
    	public String description;
        
        public static AccountForm fromUser(User user) {
        	AccountForm form = new AccountForm();
        	form.description = user.description;
        	return form;
        }
    }
}
