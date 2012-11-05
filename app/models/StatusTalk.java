package models;

import com.avaje.ebean.annotation.EnumValue;
import models.utils.Mail;
import play.i18n.Messages;

public enum StatusTalk {

    @EnumValue("R")
    REJETE("R") {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.rejected", talkTitle);
        }

        @Override
        String getMessage(String talkTitle) {
            return Messages.get("talks.statuc.mail.message.rejected", talkTitle);
        }
    },
    @EnumValue("W")
    ATTENTE("W") {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.waiting", talkTitle);
        }

        @Override
        String getMessage(String talkTitle) {
            return Messages.get("talks.statuc.mail.message.waiting", talkTitle);
        }
    },
    @EnumValue("A")
    ACCEPTE("A") {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.accepted", talkTitle);
        }

        @Override
        String getMessage(String talkTitle) {
            return Messages.get("talks.statuc.mail.message.accepted", talkTitle);
        }
    };

    public static StatusTalk fromValue(String value) {
        for (StatusTalk status : StatusTalk.values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        return null;
    }

    private StatusTalk(String interne) {
        this.interne = interne;
    }

    private String interne;

    public String getInterne(){
          return interne;
    }

    abstract String getSubject(String talkTitle);

    abstract String getMessage(String talkTitle);

    public void sendMail(Talk talk, String mail) {
        Mail.sendMail(new Mail.Envelop(getSubject(talk.title), getMessage(talk.title), mail));
    }
}
