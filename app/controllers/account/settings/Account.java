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
import controllers.Secured;

import static play.libs.Json.toJson;

@Security.Authenticated(Secured.class)
public class Account extends Controller {

    // Utilisé par le json.
    public static Result getUser(Long id) {
        User user = User.findByEmail(request().username());
        if (!user.id.equals(id)) {
            return unauthorized();
        }
        return ok(toJson(user));
    }
    
    public static Result deleteLink(Long idLink) {
    	
    	Lien lien = Lien.find.byId(idLink);
    	lien.delete();
    	
    	return ok();
    }
    
    public static Result save() {
        User user = User.findByEmail(request().username());
        Form<AccountForm> accountForm;
        List<Form<Lien>> liensForms = new ArrayList<Form<Lien>>();
        Form<Lien> newLink = null;
        String newLabel = null;
        String newUrl = null;
        JsonNode userJson = request().body().asJson();
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

        if (newLink != null
                && newLink.hasErrors()
                && (newLabel.length() > 0
                || newUrl.length() > 0)) {
            return badRequest();
        } else if (newLink != null && newLink.hasErrors()) {
        	newLink.errors().clear();
        }
        
        if (accountForm.hasErrors()) {
            return badRequest();
        }
        
        for (Form<Lien> oneLienForm : liensForms) {
        	if (oneLienForm.hasErrors()) {
                return badRequest();
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

        return ok();
    }
}
