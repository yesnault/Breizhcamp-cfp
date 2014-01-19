package controllers;

import static play.libs.Json.toJson;
import static play.data.Form.form;

import java.util.ArrayList;

import models.TalkFormat;
import models.Proposal;
import models.User;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

@SecureSocial.SecuredAction(ajaxCall = true)
public class CreneauRestController extends BaseController {

    public static Result get(Long idCreneau) {
        TalkFormat format = TalkFormat.find.byId(idCreneau);
        if (format == null) {
            return noContent();
        }
        return ok(toJson(format));
    }

    public static Result all() {
        return ok(toJson(TalkFormat.find.all()));
    }

    public static Result save() {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Form<TalkFormat> creneauForm = form(TalkFormat.class).bindFromRequest();

        if (creneauForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(creneauForm.errors())));
        }

        TalkFormat formFormat = creneauForm.get();

        if (formFormat.getId() == null) {
            // Nouveau créneau
            if (TalkFormat.findByLibelle(formFormat.getLibelle()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.creneau.already.exist"))));
            }
            formFormat.save();
        } else {
            // Mise à jour d'un créneau
            TalkFormat dbFormat = TalkFormat.find.byId(formFormat.getId());
            if (!formFormat.getLibelle().equals(dbFormat.getLibelle())
                    && TalkFormat.findByLibelle(formFormat.getLibelle()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.creneau.already.exist"))));
            }
            dbFormat.setLibelle(formFormat.getLibelle());
            dbFormat.setDureeMinutes(formFormat.getDureeMinutes());
            dbFormat.setDescription(formFormat.getDescription());
            dbFormat.setNbInstance(formFormat.getNbInstance());
            dbFormat.update();
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

        TalkFormat format = TalkFormat.find.byId(idCreneau);
        if (format != null) {
            for (Proposal proposal : new ArrayList<Proposal>(format.getProposals())) {
                format.getProposals().remove(proposal);
            }
            format.saveManyToManyAssociations("proposals");
            format.delete();
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }
}
