package models;

import com.avaje.ebean.annotation.EnumValue;
import models.utils.Mail;
import play.Configuration;
import play.i18n.Messages;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public enum StatusProposal {

    @EnumValue("R")
    REJECTED("R", "proposals.status.rejected") {
        @Override
        String getSubject(String proposalTitle) {
            return Messages.get("proposals.status.mail.subject.rejected", proposalTitle);
        }

        @Override
        String getMessage(String proposalUrl, String proposalTitle) {
            return Messages.get("proposals.status.mail.message.rejected", proposalUrl, proposalTitle);
        }
    },
    @EnumValue("W")
    WAITING("W", "proposals.status.waiting") {
        @Override
        String getSubject(String proposalTitle) {
            return Messages.get("proposals.status.mail.subject.waiting", proposalTitle);
        }

        @Override
        String getMessage(String proposalUrl, String proposalTitle) {
            return Messages.get("proposals.status.mail.message.waiting", proposalUrl, proposalTitle);
        }
    },
    @EnumValue("A")
    ACCEPTED("A", "proposals.status.accepted") {
        @Override
        String getSubject(String proposalTitle) {
            return Messages.get("proposals.status.mail.subject.accepted", proposalTitle);
        }

        @Override
        String getMessage(String proposalUrl, String proposalTitle) {
            return Messages.get("proposals.status.mail.message.accepted", proposalUrl, proposalTitle);
        }
    };

    public static StatusProposal fromValue(String value) {
        for (StatusProposal status : StatusProposal.values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        return null;
    }

    private StatusProposal(String interne, String label) {
        this.interne = interne;
        this.label = label;
    }

    private String interne;
    private String label;

    public String getInterne() {
        return interne;
    }

    public String getLabel() {
        return label;
    }

    public static StatusProposal fromCode(String code) {
        for (StatusProposal v : values()) {
            if (v.interne.equals(code)) {
                return v;
            }
        }
        return null;
    }

    abstract String getSubject(String proposalTitle);

    abstract String getMessage(String proposalUrl, String proposalTitle);

    public void sendMail(Proposal proposal, String mail) throws MalformedURLException {
        String urlString = "http://" + Configuration.root().getString("server.hostname");
        urlString += "/#/proposals/see/" + proposal.id;
        URL url = new URL(urlString);

        Set<String> emails = new HashSet<String>();
        emails.add(mail);
        for (User coSpeaker : proposal.getCoSpeakers()) {
            emails.add(coSpeaker.email);
        }

        Mail.sendMail(new Mail.Envelop(getSubject(proposal.title), getMessage(url.toString(), proposal.title), emails));
    }
}
