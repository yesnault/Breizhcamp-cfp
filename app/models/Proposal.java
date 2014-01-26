package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.EnumValue;
import com.google.common.base.Joiner;
import models.utils.BooleanUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.utils.Mail;
import play.Configuration;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;

import javax.persistence.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static models.Proposal.Status.*;

@SuppressWarnings("serial")
@Entity
public class Proposal extends Model {

    @Id
    private Long id;

    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    @Column(unique = true, length = 50)
    private String title;

    @Constraints.Required
    @Constraints.MaxLength(2000)
    @Formats.NonEmpty
    @Column(length = 2000)
    private String description;

    @Constraints.MaxLength(1000)
    @Formats.NonEmpty
    @Column(length = 1000)
    private String indicationsOrganisateurs;

    @ManyToOne
    private User speaker;

    @ManyToMany(mappedBy = "coSpeakedProposals")
    private List<User> coSpeakers = new ArrayList<User>();

    @ManyToOne
    public Event event;

    public List<User> getCoSpeakers() {
        if (coSpeakers == null) {
            coSpeakers = new ArrayList<User>();
        }
        return coSpeakers;
    }

    @OneToMany(mappedBy = "proposal")
    @JsonIgnore
    public List<Comment> comments;

    public List<Comment> getComments() {
        if (comments == null) {
            comments = new ArrayList<Comment>();
        }
        return comments;
    }

    public Status status;

    public TalkAudience audience;

    @ManyToMany(mappedBy = "proposals")
    @JsonIgnore
    public List<Tag> tags = new ArrayList<Tag>();

    public List<Tag> getTags() {
        if (tags == null) {
            tags = new ArrayList<Tag>();
        }
        return tags;
    }

    @JsonProperty(value = "tagsname")
    public String getTagsName() {
        return Joiner.on(",").join(tags);
    }

    @JsonIgnore
    public transient List<Comment> commentsFiltered;

    public void fiteredComments(User user) {
        if (user.admin) {
            commentsFiltered = new ArrayList<Comment>();
            for (Comment comment : comments) {
                if (comment.question == null) {
                    commentsFiltered.add(comment);
                }
            }
        } else {
            commentsFiltered = new ArrayList<Comment>();
            for (Comment comment : comments) {
                if (comment.question == null && BooleanUtils.isNotTrue(comment.privateComment)) {
                    commentsFiltered.add(comment);
                }
            }
        }
    }

    @JsonProperty("comments")
    public List<Comment> getCommentsFiltered() {
        return commentsFiltered;
    }

    @ManyToOne
    @Constraints.Required
    public TalkFormat format;

    @ManyToOne
    @Constraints.Required
    public Track track;

    public static Finder<Long, Proposal> find = new Finder<Long, Proposal>(Long.class, Proposal.class);

    public static List<Proposal> findAllForDisplay(Event event) {
        return find.select("id, title,  format, status, speaker.id, speaker.fullname, speaker.avatar")
                .fetch("speaker").fetch("format").where().eq("event", event).findList();
    }

    public static int countProposals(boolean draft) {
        if (draft)
            return Ebean.createSqlQuery("select count(*) as c from proposal where status='D' ").findUnique().getInteger("c");
        else
            return Ebean.createSqlQuery("select count(*) as c from proposal where status!='D' ").findUnique().getInteger("c");
    }

    public static int countProposalsAcceptes() {
        return Ebean.createSqlQuery("select count(*) as c from proposal where status='A'").findUnique().getInteger("c");
    }

    public static int countProposalsRejetes() {
        return Ebean.createSqlQuery("select count(*) as c from proposal where status='R'").findUnique().getInteger("c");
    }

    public static Proposal findByTitle(String title) {
        return find.where().eq("title", title).findUnique();
    }

    public static List<Proposal> findBySpeaker(User speaker) {
        return find.where().eq("speaker", speaker).findList();
    }

    public static List<Proposal> findBySpeakerAndEvent(User speaker, Event event) {
        return find.where().eq("speaker", speaker).eq("event", event).findList();
    }


    public static List<Proposal> findByEvent(Event event) {
        return find.where().eq("event", event).findList();
    }

    public static List<Proposal> findBySpeakerAndStatus(User speaker, Status status) {
        return find.where().eq("status", status.getInterne()).eq("speaker", speaker).findList();
    }

    public static List<Proposal> findByTrackAndStatus(Track track, Status status) {
        return find.where().eq("status", status.getInterne()).eq("track", track).findList();
    }

    public static List<Proposal> findByStatus(Status status,Event event) {
        return find.where().eq("status", status.getInterne()).eq("event", event).findList();
    }

    public static List<Proposal> findByStatusForMinimalData(Status status) {
        // proposal.id
        // proposal.title
        // proposal.description
        // proposal.speaker.fullname
        // proposal.speaker.avatar
        // proposal.speaker.description
        // proposal.speaker.links.url
        // proposal.speaker.links.label
        // proposal.coSpeakers
        return find.select("id, title, description, speaker.id, speaker.fullname, speaker.avatar, speaker.description, speaker.links, " +
                "proposal.coSpeakers.id, proposal.coSpeakers.fullname, proposal.coSpeakers.avatar, proposal.coSpeakers.description, proposal.coSpeakers.links")
                .fetch("speaker").fetch("speaker.links").fetch("coSpeakers").fetch("coSpeakers.links")
                .where().eq("status", status.getInterne()).findList();
    }

    public static Proposal findByIdWithFetch(Long id) {
        return find.select("id, title, description, speaker.id, speaker.fullname, speaker.avatar, speaker.description, speaker.links, " +
                "proposal.coSpeakers.id, proposal.coSpeakers.fullname, proposal.coSpeakers.avatar, proposal.coSpeakers.description, proposal.coSpeakers.links, proposals.tags.nom")
                .fetch("speaker").fetch("speaker.links").fetch("coSpeakers").fetch("coSpeakers.links").fetch("tags")
                .where().idEq(id).findUnique();
    }

    @JsonIgnore
    public transient Vote vote;
    public transient Double moyenne;

    @JsonProperty("note")
    public Integer note() {
        return vote == null ? 0 : vote.getNote();
    }

    public void fiteredCoSpeakers() {
        for (User coSpeaker : getCoSpeakers()) {
            coSpeaker.filterInfos();
        }
    }

    public void filtereSpeaker() {
        if (speaker != null) {
            speaker.filterInfos();
        }
    }


    public boolean isDraft() {
        return status == DRAFT;
    }


    public enum Status {

        @EnumValue("D")
        DRAFT("D", "proposals.status.draft") {
            @Override
            String getSubject(String proposalTitle) {
                return Messages.get("proposals.status.mail.subject.draft", proposalTitle);
            }

            @Override
            String getMessage(String proposalUrl, String proposalTitle) {
                return Messages.get("proposals.status.mail.message.draft", proposalUrl, proposalTitle);
            }
        },
        @EnumValue("S")
        SUBMITTED("S", "proposals.status.submitted") {
            @Override
            String getSubject(String proposalTitle) {
                return Messages.get("proposals.status.mail.subject.submitted", proposalTitle);
            }

            @Override
            String getMessage(String proposalUrl, String proposalTitle) {
                return Messages.get("proposals.status.mail.message.rejected", proposalUrl, proposalTitle);
            }
        },
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

        public static Status fromValue(String value) {
            for (Status status : Status.values()) {
                if (status.name().equals(value)) {
                    return status;
                }
            }
            return null;
        }

        private Status(String interne, String label) {
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

        public static Status fromCode(String code) {
            for (Status v : values()) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndicationsOrganisateurs() {
        return indicationsOrganisateurs;
    }

    public void setIndicationsOrganisateurs(String indicationsOrganisateurs) {
        this.indicationsOrganisateurs = indicationsOrganisateurs;
    }

    public User getSpeaker() {
        return speaker;
    }

    public void setSpeaker(User speaker) {
        this.speaker = speaker;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public TalkFormat getFormat() {
        return format;
    }

    public void setFormat(TalkFormat format) {
        this.format = format;
    }

    public TalkAudience getAudience() {
        return audience;
    }

    public void setAudience(TalkAudience audience) {
        this.audience = audience;
    }

    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        this.vote = vote;
    }

    public Double getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(Double moyenne) {
        this.moyenne = moyenne;
    }
}