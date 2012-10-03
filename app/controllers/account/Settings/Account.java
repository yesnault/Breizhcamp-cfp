package controllers.account.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Lien;
import models.User;
import play.data.Form;
import play.data.format.Formats;
import play.data.validation.Constraints;
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
        
        List<Form<Lien>> liensForms = new ArrayList<Form<Lien>>();
        
        for (Lien lien : user.getLiens()) {
        	liensForms.add(form(Lien.class).fill(lien));
        }
        
        Form<Lien> newLink = form(Lien.class);
        
        return ok(account.render(user, accountForm, liensForms, newLink));
    }
    
    public static Result deleteLink(Long idLink) {
    	
    	Lien lien = Lien.find.byId(idLink);
    	lien.delete();
    	
    	return redirect(controllers.account.settings.routes.Account.index());
    }
    
    public static Result save() {
        User user = User.findByEmail(request().username());
        
        Form<AccountForm> accountForm = form(AccountForm.class).bindFromRequest();


        List<Form<Lien>> liensForms = new ArrayList<Form<Lien>>();
        
        int countLien = 0;
        
        String[] labels = request().body().asFormUrlEncoded().get("label");
        String[] urls = request().body().asFormUrlEncoded().get("url");
        
        for (countLien = 0; countLien < user.getLiens().size(); countLien++) {
        	Map<String, String> data = new HashMap<String, String>(2);
        	data.put("label", labels[countLien]);
        	data.put("url", urls[countLien]);
        	liensForms.add(form(Lien.class).bind(data));
        }

    	Map<String, String> data = new HashMap<String, String>(2);
    	data.put("label", labels[countLien]);
    	data.put("url", urls[countLien]);

        Form<Lien> newLink = form(Lien.class).bind(data);

        
        if (newLink.hasErrors()
        		&& (labels[countLien].length() > 0
        			|| urls[countLien].length() > 0)) {
        	return badRequest(account.render(user, accountForm, liensForms, newLink));
        } else if (newLink.hasErrors()) {
        	newLink.errors().clear();
        }
        
        if (accountForm.hasErrors()) {
        	return badRequest(account.render(user, accountForm, liensForms, newLink));
        }
        
        for (Form<Lien> oneLienForm : liensForms) {
        	if (oneLienForm.hasErrors()) {
            	return badRequest(account.render(user, accountForm, liensForms, newLink));
        	}
        }
        
        for (Lien oneLien : user.getLiens()) {
        	Form<Lien> lienForm = liensForms.remove(0);
        	oneLien.label = lienForm.get().label;
        	oneLien.url = lienForm.get().url;
        }
        
        user.description = accountForm.get().description;
        
        if (labels[countLien].length() > 0
    			|| urls[countLien].length() > 0) {
        	Lien lien = newLink.get();
        	user.getLiens().add(lien);
        }
        
        user.save();
                
        return redirect(controllers.account.settings.routes.Account.index());	
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
