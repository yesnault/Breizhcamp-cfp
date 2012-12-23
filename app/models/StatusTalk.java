package models;

import com.avaje.ebean.annotation.EnumValue;
import models.utils.Mail;
import play.Configuration;
import play.i18n.Messages;

import java.net.MalformedURLException;
import java.net.URL;

public enum StatusTalk {

    @EnumValue("R")
    REJETE("R") {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.rejected", talkTitle);
        }

        @Override
        String getMessage(String talkUrl,String talkTitle) {
            return Messages.get("talks.status.mail.message.rejected",talkUrl, talkTitle);
        }
    },
    @EnumValue("W")
    ATTENTE("W") {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.waiting", talkTitle);
        }

        @Override
        String getMessage(String talkUrl,String talkTitle) {
            return Messages.get("talks.status.mail.message.waiting",talkUrl, talkTitle);
        }
    },
    @EnumValue("A")
    ACCEPTE("A") {
        @Override
        String getSubject(String talkTitle) {
            return Messages.get("talks.status.mail.subject.accepted", talkTitle);
        }

        @Override
        String getMessage(String talkUrl,String talkTitle) {
            return Messages.get("talks.status.mail.message.accepted",talkUrl, talkTitle);
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

    public String getInterne() {
        return interne;
    }

    public static StatusTalk fromCode(String code) {
        for (StatusTalk v : values()) {
            if (v.interne.equals(code)) {
                return v;
            }
        }
        return null;
    }

    abstract String getSubject(String talkTitle);

    abstract String getMessage(String talkUrl,String talkTitle);

    public void sendMail(Talk talk, String mail) throws MalformedURLException {
        String urlString = "http://" + Configuration.root().getString("server.hostname");
        urlString += "/#/talks/see/" + talk.id;
        URL url = new URL(urlString);

        Mail.sendMail(new Mail.Envelop(getSubject(talk.title), getMessage(url.toString(),talk.title), mail));
    }
}
