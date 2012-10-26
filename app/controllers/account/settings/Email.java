package controllers.account.settings;

import controllers.Secured;
import models.Token;
import models.User;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.net.MalformedURLException;

/**
 * Settings -> Email page.
 * <p/>
 * User: yesnault
 * Date: 23/06/12
 */
@Security.Authenticated(Secured.class)
public class Email extends Controller {

    public static class AskForm {
        @Constraints.Required
        public String email;
    }

    /**
     * Send a mail to confirm.
     *
     * @return email page with flash error or success
     */
    public static Result runEmail() {
        JsonNode jsonNode = request().body().asJson();
        Form<AskForm> askForm = form(AskForm.class).bind(jsonNode);
        User user = User.findByEmail(request().username());

        if (askForm.hasErrors()) {
            return badRequest();
        }

        try {
            String mail = askForm.get().email;
            Token.sendMailChangeMail(user, mail);
            return ok();
        } catch (MalformedURLException e) {
            Logger.error("Cannot validate URL", e);
            flash("error", Messages.get("error.technical"));
        }
        return badRequest();
    }

    /**
     * Validate a email.
     *
     * @return email page with flash error or success
     */
    public static Result validateEmail(String token) {
        User user = User.findByEmail(request().username());

        if (token == null) {
            return badRequest();
        }

        Token resetToken = Token.findByTokenAndType(token, Token.TypeToken.email);
        if (resetToken == null) {
            return badRequest();
        }

        if (resetToken.isExpired()) {
            resetToken.delete();
            return badRequest();
        }

        user.email = resetToken.email;
        user.save();

        session("email", resetToken.email);

        return ok();
    }
}
