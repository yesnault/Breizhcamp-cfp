package controllers;

import models.Event;
import models.Talk;
import models.User;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class EventRestController extends Controller {

    private static User getLoggedUser() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.findByExternalId(socialUser.identityId().userId(), socialUser.identityId().providerId());
        return user;
    }

    public static Result get(Long idEvent) {
        Event event = Event.find.byId(idEvent);
        if (event == null) {
            return noContent();
        }
        return ok(toJson(event));
    }

    public static Result close(Long idEvent) {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Event event = Event.find.byId(idEvent);
        if (event != null) {
            event.setClos(!event.isClos());
            event.update();
            return ok(toJson(event));
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    public static Result all() {
        return ok(toJson(Event.find.all()));
    }

    public static Result save() {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Form<Event> evenForm = form(Event.class).bindFromRequest();

        if (evenForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(evenForm.errors())));
        }

        Event formEvent = evenForm.get();

        if (formEvent.getId() == null) {
            // Nouvel Evénement
            if (Event.findByName(formEvent.getName()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.event.already.exist"))));
            }
            formEvent.save();
        } else {
            // Mise à jour d'un événement
            Event dbEvent = Event.find.byId(formEvent.getId());
            if (!formEvent.getName().equals(dbEvent.getName())
                    && Event.findByName(formEvent.getName()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.event.already.exist"))));
            }
            dbEvent.setClos(formEvent.isClos());
            dbEvent.setDescription(formEvent.getDescription());
            dbEvent.update();
        }

        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    public static Result delete(Long idEvent) {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Event event = Event.find.byId(idEvent);
        if (event != null) {
            List<Talk> talks = Talk.findByEvent(event);
            if (talks.isEmpty() && event.isClos()) {
                event.delete();
            } else {
                Map<String, List<String>> errors = new HashMap<String, List<String>>();
                errors.put("event", Collections.singletonList(Messages.get("error.event.hasElement")));
                return badRequest(toJson(errors));
            }


        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }
}
