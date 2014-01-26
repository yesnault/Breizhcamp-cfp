package controllers;

import models.Proposal;
import models.TalkFormat;
import models.User;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.util.ArrayList;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class FormatRestController extends BaseController {

    public static Result get(Long id) {
        TalkFormat format = TalkFormat.find.byId(id);
        if (format == null || !format.getEvent().equals(getEvent())) {
            return noContent();
        }
        return ok(toJson(format));
    }

    public static Result all() {
        return ok(toJson(TalkFormat.findByEvent(getEvent())));
    }

    public static Result save() {

        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin && !user.hasEvent(getEvent())) {
            return forbidden();
        }

        Form<TalkFormat> form = form(TalkFormat.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(form.errors())));
        }

        TalkFormat formFormat = form.get();

        if (formFormat.getId() == null) {
            // Nouveau format
            if (TalkFormat.findByLibelle(formFormat.getLibelle(),getEvent()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.format.already.exist"))));
            }
            formFormat.setEvent(getEvent());
            formFormat.save();
        } else {
            // Mise à jour d'un format
            TalkFormat dbFormat = TalkFormat.find.byId(formFormat.getId());
            if (!formFormat.getLibelle().equals(dbFormat.getLibelle())
                    && TalkFormat.findByLibelle(formFormat.getLibelle(),getEvent()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.format.already.exist"))));
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

    
    public static Result delete(Long id) {
        
        // Vérification du rôle d'admin
        User user = getLoggedUser();
        if (!user.admin && !user.hasEvent(getEvent())) {
            return forbidden();
        }

        TalkFormat format = TalkFormat.find.byId(id);
        if (format != null && format.getEvent().equals(getEvent())) {
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
