package controllers.account.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import controllers.BaseController;
import models.*;
import models.utils.TransformValidationErrors;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.util.*;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class Account extends BaseController {

    // Utilisé par le json.
    public static Result getUser(Long id) {
        User user = getLoggedUser();
        if (!user.id.equals(id)) {
            return unauthorized();
        }
        return ok(toJson(user));
    }


    public static Result save() {
        User user = getLoggedUser();
        Form<AccountForm> accountForm;
        List<Form<Link>> liensForms = new ArrayList<Form<Link>>();
        Form<Link> newLink = null;
        String newLabel = null;
        String newUrl = null;
        JsonNode userJson = request().body().asJson();
        accountForm = form(AccountForm.class).bind(userJson);

        // Parcour des links du user;
        ArrayNode liens = (ArrayNode) userJson.get("links");
        for (JsonNode lien : liens) {
            if (lien.get("id") != null) {
                Form<Link> oneLienForm = form(Link.class).bind(lien);
                if (oneLienForm.hasErrors()) {
                    Map<String, Map<String, List<String>>> errors = new HashMap<String, Map<String, List<String>>>();
                    errors.put(lien.get("id").asText(), TransformValidationErrors.transform(oneLienForm.errors()));
                    return badRequest(toJson(errors));
                }
                liensForms.add(oneLienForm);
            } else {
                newLink = form(Link.class).bind(lien);
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

        for (Link oneLink : user.getLinks()) {
            Form<Link> lienForm = liensForms.remove(0);
            oneLink.label = lienForm.get().label;
            oneLink.url = lienForm.get().url;
        }

        user.description = accountForm.get().description;
        user.avatar = accountForm.get().avatar;

        if (newLinkExists(newLink, newLabel, newUrl)) {
            Link link = newLink.get();
            user.getLinks().add(link);
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

    public static boolean newLinkExists(Form<Link> newLink, String newLabel, String newUrl) {
        return newLink != null
                && ((newLabel != null && newLabel.length() > 0)
                || (newUrl != null && newUrl.length() > 0));
    }

    public static Result saveEmail() {
        JsonNode node = request().body().asJson();
        String email = node.get("email").asText();

        if (email == null || email.equals("")) {
            Logger.debug("error.email.required");
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("email", Collections.singletonList(Messages.get("error.email.already.exist")));
            return badRequest(toJson(errors));
        }

        User user = getLoggedUser();
        User existUser = User.findByEmail(email);
        if (existUser != null && !existUser.equals(user)) {
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
