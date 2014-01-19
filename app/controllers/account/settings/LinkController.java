package controllers.account.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.BaseController;
import models.Link;
import models.LinkType;
import models.User;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class LinkController extends BaseController {

    public static Result deleteLink(Long idLink) {

        Link link = Link.find.byId(idLink);
        link.delete();

        return ok();
    }

    public static Result getTypes() {
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);

        for(LinkType linkType : LinkType.values()){
            ObjectNode typeJson = Json.newObject();
            typeJson.put("id", linkType.name());
            typeJson.put("label", linkType.getLabel());
            typeJson.put("icon", linkType.getIcon());
            typeJson.put("url", linkType.getUrl());
            result.add(typeJson);
        }




        return ok(result);
    }

    public static Result save() {
        User user = getLoggedUser();
        List<Form<Link>> liensForms = new ArrayList<Form<Link>>();
        Form<Link> newLink = null;
        String newLabel = null;
        String newUrl = null;
        LinkType newLinkType = LinkType.OTHER;
        JsonNode userJson = request().body().asJson();

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
                if (lien.get("type") != null) {
                    newLinkType = LinkType.valueOf(lien.get("type").asText());
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

        for (Link oneLink : user.getLinks()) {
            Form<Link> lienForm = liensForms.remove(0);
            oneLink.label = lienForm.get().label;
            oneLink.url = lienForm.get().url;
            //TODO gestion du type
        }

        if (newLinkExists(newLink, newLabel, newUrl)) {
            Link link = newLink.get();
            link.linkType =  newLinkType;
            user.getLinks().add(link);
        }

        user.save();

        return ok();
    }


    public static boolean newLinkExists(Form<Link> newLink, String newLabel, String newUrl) {
        return newLink != null
                && ((newLabel != null && newLabel.length() > 0)
                || (newUrl != null && newUrl.length() > 0));
    }
}
