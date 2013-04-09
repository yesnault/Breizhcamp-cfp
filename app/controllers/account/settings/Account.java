package controllers.account.settings;

import static play.libs.Json.toJson;

import java.util.*;

import models.DynamicField;
import models.DynamicFieldJson;
import models.DynamicFieldValue;
import models.Lien;
import models.User;
import models.utils.TransformValidationErrors;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import static play.mvc.Controller.request;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

@SecureSocial.SecuredAction(ajaxCall=true)
public class Account extends Controller {

    public static User getLoggedUser() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.findByExternalId(socialUser.id().id(), socialUser.id().providerId());
        return user;
    }

    // Utilisé par le json.
    public static Result getUser(Long id) {
        User user = getLoggedUser();
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
        User user = getLoggedUser();
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
                Form<Lien> oneLienForm = form(Lien.class).bind(lien);
                if (oneLienForm.hasErrors()) {
                    Map<String, Map<String, List<String>>> errors = new HashMap<String, Map<String, List<String>>>();
                    errors.put(lien.get("id").asText(), TransformValidationErrors.transform(oneLienForm.errors()));
                    return badRequest(toJson(errors));
                }
                liensForms.add(oneLienForm);
            } else {
                newLink = form(Lien.class).bind(lien);
                if (lien.get("label") != null) {
                    newLabel = lien.get("label").asText();
                }
                if (lien.get("url") != null) {
                    newUrl = lien.get("url").asText();
                }
            }
        }

        if (newLinkExists(newLink, newLabel, newUrl)
                && newLink.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(newLink.errors())));
        }
        if (newLink != null && newLink.hasErrors()) {
        	newLink.errors().clear();
        }
        
        if (accountForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(accountForm.errors())));
        }
        
        for (Lien oneLien : user.getLiens()) {
        	Form<Lien> lienForm = liensForms.remove(0);
        	oneLien.label = lienForm.get().label;
        	oneLien.url = lienForm.get().url;
        }
        
        user.description = accountForm.get().description;
        user.avatar = accountForm.get().avatar;
        
        if (newLinkExists(newLink, newLabel, newUrl)) {
        	Lien lien = newLink.get();
        	user.getLiens().add(lien);
        }

        user.save();

        for (DynamicFieldJson fieldJson : accountForm.get().getDynamicFields()) {
            if (fieldJson.getIdValue() != null) {
                // Edit case
                DynamicFieldValue valueToEdit = DynamicFieldValue.find.byId(fieldJson.getIdValue());
                valueToEdit.setValue(fieldJson.getValue());
                valueToEdit.update();
            } else {
                // New field case
                if (fieldJson.getValue() != null && fieldJson.getValue().length() > 0) {
                    DynamicFieldValue newValue = new DynamicFieldValue();
                    newValue.setValue(fieldJson.getValue());
                    newValue.setDynamicField(DynamicField.find.byId(fieldJson.getIdField()));
                    newValue.setUser(user);
                    newValue.save();
                }
            }
        }

        return ok();
    }

    public static class MacForm {
        public String adresseMac;
    }

    public static Result changeMac() {
        JsonNode jsonNode = request().body().asJson();
        Form<MacForm> macForm = form(MacForm.class).bind(jsonNode);
        User user = getLoggedUser();

        if (macForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(macForm.errors())));
        }

        String adresseMac = macForm.get().adresseMac;

        user.adresseMac = adresseMac;

        user.save();

        return ok();
    }

    public static boolean newLinkExists(Form<Lien> newLink, String newLabel, String newUrl) {
        return newLink != null
                && ((newLabel != null && newLabel.length() > 0)
                || (newUrl != null && newUrl.length() > 0));
    }
    
    public static Result saveEmail() {
        JsonNode node = request().body().asJson();
        String email = node.get("email").asText();

        if(email == null || email.equals("") ){
            Logger.debug("error.email.required");
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("email", Collections.singletonList(Messages.get("error.email.already.exist")));
            return badRequest(toJson(errors));
        }

        User user = getLoggedUser();
        User existUser = User.findByEmail(email);
        if(existUser != null && !existUser.equals(user)){
            Logger.debug("error.email.already.exist");
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("email", Collections.singletonList(Messages.get("error.email.already.exist")));
            return badRequest(toJson(errors));
        }



        user.email = email;
        user.save();
        return noContent();
    }
    
}
