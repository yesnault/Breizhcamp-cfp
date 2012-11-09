package controllers;

import models.*;
import models.utils.TransformValidationErrors;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.ArrayList;

import static play.libs.Json.toJson;


@Security.Authenticated(Secured.class)
public class DynamicFieldRestController extends Controller {
	
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
        User user = User.findByEmail(request().username());
        if (!user.admin) {
            return unauthorized();
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
        User user = User.findByEmail(request().username());
        if (!user.admin) {
            return unauthorized();
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
