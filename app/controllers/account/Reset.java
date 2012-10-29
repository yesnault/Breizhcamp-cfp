package controllers.account;

import models.Token;
import models.User;
import models.utils.AppException;
import models.utils.Mail;
import org.apache.commons.mail.EmailException;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import java.net.MalformedURLException;

/**
 * Token password :
 * - ask for an email address.
 * - send a link pointing them to a reset page.
 * - show the reset page and set them reset it.
 * <p/>
 * <p/>
 * User: yesnault
 * Date: 20/01/12
 */
public class Reset extends Controller {

    public static class AskForm {
        @Constraints.Required
        public String email;
    }

    public static class ResetForm {
        @Constraints.Required
        public String inputPassword;
    }

    /**
     * Run ask password.
     *
     * @return reset password form if error, runAsk render otherwise
     */
    public static Result runAsk() {
        JsonNode requestJson = request().body().asJson();
        Form<AskForm> askForm;

        if (requestJson == null) {
            askForm = form(AskForm.class).bindFromRequest();
        } else {
            askForm = form(AskForm.class).bind(requestJson);
        }

        if (askForm.hasErrors()) {
            return badRequest();
        }

        final String email = askForm.get().email;
        Logger.debug("runAsk: email = " + email);
        User user = User.findByEmail(email);
        Logger.debug("runAsk: user = " + user);

        // If we do not have this email address in the list, we should not expose this to the user.
        // This exposes that the user has an account, allowing a user enumeration attack.
        // See http://www.troyhunt.com/2012/05/everything-you-ever-wanted-to-know.html for details.
        // Instead, email the person saying that the reset failed.
        if (user == null) {
            Logger.debug("No user found with email " + email);
            sendFailedPasswordResetAttempt(email);
            return badRequest();
        }

        Logger.debug("Sending password reset link to user " + user);

        try {
            Token.sendMailResetPassword(user);
            return ok();
        } catch (MalformedURLException e) {
            Logger.error("Cannot validate URL", e);
        }
        return badRequest();
    }

    /**
     * Sends an email to say that the password reset was to an invalid email.
     *
     * @param email the email address to send to.
     */
    private static void sendFailedPasswordResetAttempt(String email) {
        String subject = Messages.get("mail.reset.fail.subject");
        String message = Messages.get("mail.reset.fail.message", email);

        Mail.Envelop envelop = new Mail.Envelop(subject, message, email);
        Mail.sendMail(envelop);
    }

    /**
     * @return reset password form
     */
    public static Result runReset(String token) {
        Form<ResetForm> resetForm = form(ResetForm.class).bindFromRequest();

        if (resetForm.hasErrors()) {
            return badRequest();
        }

        try {
            Token resetToken = Token.findByTokenAndType(token, Token.TypeToken.password);
            if (resetToken == null) {
                return badRequest();
            }

            if (resetToken.isExpired()) {
                resetToken.delete();
                return badRequest();
            }

            // check email
            User user = User.find.byId(resetToken.userId);
            if (user == null) {
                // display no detail (email unknown for example) to
                // avoir check email by foreigner
                return badRequest();
            }

            String password = resetForm.get().inputPassword;
            user.changePassword(password);

            // Send email saying that the password has just been changed.
            sendPasswordChanged(user);
            return ok();
        } catch (AppException e) {
            return badRequest();
        } catch (EmailException e) {
            return badRequest();
        }

    }

    /**
     * Send mail with the new password.
     *
     * @param user user created
     * @throws EmailException Exception when sending mail
     */
    private static void sendPasswordChanged(User user) throws EmailException {
        String subject = Messages.get("mail.reset.confirm.subject");
        String message = Messages.get("mail.reset.confirm.message");
        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.email);
        Mail.sendMail(envelop);
    }
}
