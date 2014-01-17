package controllers;

import static play.libs.Json.toJson;
import static play.data.Form.form;

import models.DynamicField;
import models.DynamicFieldValue;
import models.User;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

@SecureSocial.SecuredAction(ajaxCall = true)
public class DynamicFieldRestController extends BaseController {

    public static Result get(Long idDynamicField) {
        DynamicField dynamicField = DynamicField.find.byId(idDynamicField);
        if (dynamicField == null) {
            return noContent();
        }
        return ok(toJson(dynamicField));
    }

    public static Result all() {
        return ok(toJson(DynamicField.find.all()));
    }

    public static Result save() {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Form<DynamicField> dynamicFieldForm = form(DynamicField.class).bindFromRequest();

        if (dynamicFieldForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(dynamicFieldForm.errors())));
        }

        DynamicField formDynamicField = dynamicFieldForm.get();

        if (formDynamicField.getId() == null) {
            // Nouveau dynamicField
            if (DynamicField.findByName(formDynamicField.getName()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.dynamicField.already.exist"))));
            }
            formDynamicField.save();
        } else {
            // Mise à jour d'un dynamicField
            DynamicField dbDynamicField = DynamicField.find.byId(formDynamicField.getId());
            if (!formDynamicField.getName().equals(dbDynamicField.getName())
                    && DynamicField.findByName(formDynamicField.getName()) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.dynamicField.already.exist"))));
            }
            dbDynamicField.setName(formDynamicField.getName());
            dbDynamicField.update();
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    public static Result delete(Long idDynamicField) {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        DynamicField dynamicField = DynamicField.find.byId(idDynamicField);
        if (dynamicField != null) {
            for (DynamicFieldValue value : dynamicField.getDynamicFieldValues()) {
                value.delete();
            }
            dynamicField.delete();
        }
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }
}
