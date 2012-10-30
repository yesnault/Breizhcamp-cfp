package models;

import models.utils.Mail;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

public enum StatusTalk {

    REJETE {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.rejected", talkTitle);
        }

        @Override
        String getMessage(String talkTitle) {
            return Messages.get("talks.statuc.mail.message.rejected", talkTitle);
        }
    },
    ATTENTE {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.waiting", talkTitle);
        }

        @Override
        String getMessage(String talkTitle) {
            return Messages.get("talks.statuc.mail.message.waiting", talkTitle);
        }
    },
    ACCEPTE {
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

    abstract String getSubject(String talkTitle);
    abstract String getMessage(String talkTitle);

    public void sendMail(Talk talk, String mail) {
        Mail.sendMail(new Mail.Envelop(getSubject(talk.title), getMessage(talk.title), mail));
    }
}
