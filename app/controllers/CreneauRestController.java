package controllers;

import static play.libs.Json.toJson;

import java.util.ArrayList;

import models.Creneau;
import models.Talk;
import models.User;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

@SecureSocial.SecuredAction(ajaxCall = true)
public class CreneauRestController extends Controller {

    private static User getLoggedUser() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.findByExternalId(socialUser.id().id(), socialUser.id().providerId());
        return user;
    }

    public static Result get(Long idCreneau) {
        Creneau creneau = Creneau.find.byId(idCreneau);
        if (creneau == null) {
            return noContent();
        }
        return ok(toJson(creneau));
    }

    public static Result all() {
        return ok(toJson(Creneau.find.all()));
    }

    public static Result save() {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Form<Creneau> creneauForm = form(Creneau.class).bindFromRequest();

        if (creneauForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(creneauForm.errors())));
        }

        Creneau formCreneau = creneauForm.get();

        if (formCreneau.getId() == null) {
            // Nouveau créneau
            if (Creneau.findByLibelle(formCreneau.getLibelle()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.creneau.already.exist"))));
            }
            formCreneau.save();
        } else {
            // Mise à jour d'un créneau
            Creneau dbCreneau = Creneau.find.byId(formCreneau.getId());
            if (!formCreneau.getLibelle().equals(dbCreneau.getLibelle())
                    && Creneau.findByLibelle(formCreneau.getLibelle()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.creneau.already.exist"))));
            }
            dbCreneau.setLibelle(formCreneau.getLibelle());
            dbCreneau.setDureeMinutes(formCreneau.getDureeMinutes());
            dbCreneau.setDescription(formCreneau.getDescription());
            dbCreneau.setNbInstance(formCreneau.getNbInstance());
            dbCreneau.update();
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    
    public static Result delete(Long idCreneau) {
        
        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Creneau creneau = Creneau.find.byId(idCreneau);
        if (creneau != null) {
            for (Talk talk : new ArrayList<Talk>(creneau.getTalks())) {
                creneau.getTalks().remove(talk);
            }
            creneau.saveManyToManyAssociations("talks");
            creneau.delete();
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }
}
