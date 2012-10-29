package models.utils;

import play.data.validation.ValidationError;
import play.i18n.Messages;

import java.util.*;

public class TransformValidationErrors {

    public static Map<String, List<String>> transform(Map<String, List<ValidationError>> validationErrors) {
        if (validationErrors == null) {
            return null;
        }
        Map<String, List<String>> errors = new HashMap<String, List<String>>();
        for (Map.Entry<String, List<ValidationError>> entry : validationErrors.entrySet()) {
            errors.put(entry.getKey(), new ArrayList<String>());
            for (ValidationError oneError : entry.getValue()) {
                errors.get(entry.getKey()).add(Messages.get(oneError.message()));
            }
        }
        return errors;
    }

    public static Map<String, List<String>> transform(String oneError) {

        Map<String, List<String>> errors = new HashMap<String, List<String>>();
        errors.put("", Collections.singletonList(oneError));
        return errors;
    }
}
