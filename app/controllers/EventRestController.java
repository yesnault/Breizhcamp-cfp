package controllers;

import models.Event;
import models.Proposal;
import models.User;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.util.*;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class EventRestController extends BaseController {

    public static Result get(Long idEvent) {
        Event event = Event.find.byId(idEvent);
        if (event == null) {
            return noContent();
        }
        return ok(toJson(event));
    }

    public static Result organizers(Long idEvent) {
        Event event = Event.find.byId(idEvent);
        if (event == null) {
            return noContent();
        }
        return ok(toJson(event.getOrganizers()));
    }

    public static Result getCurrentEvent() {
        return ok(toJson(getEvent()));
    }

    public static Result close(Long idEvent) {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Event event = Event.find.byId(idEvent);
        if (event != null) {

            //TODO gestion de l'agenda

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
            List<User> organizersInDb = new ArrayList<User>();
            for (User organizer : formEvent.getOrganizers()) {
                organizersInDb.add(User.findById(organizer.id));
            }
            formEvent.getOrganizers().clear();
            formEvent.getOrganizers().addAll(organizersInDb);
            formEvent.saveManyToManyAssociations("organizers");
            formEvent.update();

        } else {
            // Mise à jour d'un événement
            Event dbEvent = Event.find.byId(formEvent.getId());
            if (!formEvent.getName().equals(dbEvent.getName())
                    && Event.findByName(formEvent.getName()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.event.already.exist"))));
            }

            dbEvent.setUrl(formEvent.getUrl());
            dbEvent.setShortName(formEvent.getShortName());
            dbEvent.setCgu(formEvent.getCgu());
            dbEvent.setDescription(formEvent.getDescription());
            dbEvent.update();

            updateOrganizers(formEvent, dbEvent);
        }

        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    private static void updateOrganizers(Event formEvent, Event dbEvent) {
        Set<Long> organizersInForm = new HashSet<Long>();
        for (User organizer : formEvent.getOrganizers()) {
            organizersInForm.add(organizer.id);
        }
        List<User> organizersTmp = new ArrayList<User>(dbEvent.getOrganizers());
        Set<Long> organizersInDb = new HashSet<Long>();
        for (User organizer : organizersTmp) {
            if (!organizersInForm.contains(organizer.id)) {
                dbEvent.getOrganizers().remove(organizer);
            } else {
                organizersInDb.add(organizer.id);
            }
        }

        for (Long organizer : organizersInForm) {
            if (!organizersInDb.contains(organizer)) {
                dbEvent.getOrganizers().add(User.findById(organizer));
            }
        }
        dbEvent.saveManyToManyAssociations("organizers");
    }

    public static Result delete(Long idEvent) {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Event event = Event.find.byId(idEvent);
        if (event != null) {
            List<Proposal> proposals = Proposal.findByEvent(event);
            if (proposals.isEmpty()) {
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
