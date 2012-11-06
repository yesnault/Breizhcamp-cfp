package controllers;

import models.*;
import models.utils.TransformValidationErrors;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.*;

import static play.libs.Json.toJson;


@Security.Authenticated(Secured.class)
public class CreneauRestController extends Controller {
	
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
        User user = User.findByEmail(request().username());
        if (!user.admin) {
            return unauthorized();
        }

        System.out.println(request().body().asText());

        Form<Creneau> creneauForm = form(Creneau.class).bindFromRequest();

        if (creneauForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(creneauForm.errors())));
        }

        Creneau formCreneau = creneauForm.get();

        if (formCreneau.getId() == null) {
            // Nouveau talk
            if (Creneau.findByLibelle(formCreneau.getLibelle()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.creneau.already.exist"))));
            }
            formCreneau.save();
        } else {
            // Mise à jour d'un talk
            Creneau dbCreneau = Creneau.find.byId(formCreneau.getId());
            if (!formCreneau.getLibelle().equals(dbCreneau.getLibelle())
                    && Talk.findByTitle(formCreneau.getLibelle()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.creneau.already.exist"))));
            }
            dbCreneau.setLibelle(formCreneau.getLibelle());
            dbCreneau.setDureeMinutes(formCreneau.getDureeMinutes());
            dbCreneau.update();
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
	}
	
	public static Result delete(Long idCreneau) {
        User user = User.findByEmail(request().username());
        if (!user.admin) {
            return unauthorized();
        }

		Creneau creneau = Creneau.find.byId(idCreneau);
        if (creneau != null) {
            creneau.delete();
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

}
