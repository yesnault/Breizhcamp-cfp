package controllers.account.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Lien;
import models.User;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import play.Logger;
import play.data.Form;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.account.settings.account;
import controllers.Secured;

import static play.libs.Json.toJson;

@Security.Authenticated(Secured.class)
public class Account extends Controller {

    public static Result getUser(Long id) {
        User user = User.findByEmail(request().username());
        if (!user.id.equals(id)) {
            return unauthorized();
        }
        return ok(toJson(user));
    }

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
        Form<AccountForm> accountForm;
        List<Form<Lien>> liensForms = new ArrayList<Form<Lien>>();
        Form<Lien> newLink = null;
        String newLabel = null;
        String newUrl = null;
        JsonNode userJson = request().body().asJson();
        boolean isJson = false;
        if (userJson != null) {
            isJson = true;
            accountForm = form(AccountForm.class).bind(userJson);

            // Parcour des liens du user;
            ArrayNode liens = (ArrayNode) userJson.get("liens");
            for (JsonNode lien : liens ) {
                if (lien.get("id") != null) {
                    liensForms.add(form(Lien.class).bind(lien));
                } else {
                    newLink = form(Lien.class).bind(lien);
                    newLabel = lien.get("label").asText();
                    newUrl = lien.get("url").asText();
                }
            }
        } else {
            accountForm = form(AccountForm.class).bindFromRequest();


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

            newLink = form(Lien.class).bind(data);
            newLabel = labels[countLien];
            newUrl = urls[countLien];
        }


        if (newLink != null
                && newLink.hasErrors()
                && (newLabel.length() > 0
                || newUrl.length() > 0)) {
            if (isJson) {
                return badRequest();
            } else {
                return badRequest(account.render(user, accountForm, liensForms, newLink));
            }
        } else if (newLink != null && newLink.hasErrors()) {
        	newLink.errors().clear();
        }
        
        if (accountForm.hasErrors()) {
            if (isJson) {
                return badRequest();
            }
        	return badRequest(account.render(user, accountForm, liensForms, newLink));
        }
        
        for (Form<Lien> oneLienForm : liensForms) {
        	if (oneLienForm.hasErrors()) {
                if (isJson) {
                    return badRequest();
                }
            	return badRequest(account.render(user, accountForm, liensForms, newLink));
        	}
        }
        
        for (Lien oneLien : user.getLiens()) {
        	Form<Lien> lienForm = liensForms.remove(0);
        	oneLien.label = lienForm.get().label;
        	oneLien.url = lienForm.get().url;
        }
        
        user.description = accountForm.get().description;
        
        if (newLink != null && (newLabel.length() > 0
    			|| newUrl.length() > 0)) {
        	Lien lien = newLink.get();
        	user.getLiens().add(lien);
        }
        
        user.save();

        if (isJson) {
            return ok();
        }
        return redirect(controllers.account.settings.routes.Account.index());	
    }
}
