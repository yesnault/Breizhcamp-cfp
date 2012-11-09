package controllers.account.settings;

import models.DynamicFieldJson;
import models.User;
import play.data.format.Formats;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

public class AccountForm {
    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(2000)
    public String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private List<DynamicFieldJson> dynamicFields;

    public List<DynamicFieldJson> getDynamicFields() {
        if (dynamicFields == null) {
            dynamicFields = new ArrayList<DynamicFieldJson>();
        }
        return dynamicFields;
    }
}
