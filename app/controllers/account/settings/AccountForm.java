package controllers.account.settings;

import models.User;
import play.data.format.Formats;
import play.data.validation.Constraints;

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

    public static AccountForm fromUser(User user) {
        AccountForm form = new AccountForm();
        form.description = user.description;
        return form;
    }
}
