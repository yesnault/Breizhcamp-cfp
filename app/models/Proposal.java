package models;

import com.avaje.ebean.Ebean;
import com.google.common.base.Joiner;
import models.utils.BooleanUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Entity
public class Proposal extends Model {

    @Id
    public Long id;
    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    @Column(unique = true, length = 50)
    public String title;
    @Constraints.Required
    @Constraints.MaxLength(2000)
    @Formats.NonEmpty
    @Column(length = 2000)
    public String description;

    @Constraints.MaxLength(1000)
    @Formats.NonEmpty
    @Column(length = 1000)
    public String indicationsOrganisateurs;

    @ManyToOne
    public User speaker;
    @ManyToMany(mappedBy = "coSpeakedProposals")
    public List<User> coSpeakers;

    public boolean draft;

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
    public StatusProposal statusProposal;
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
    public Format dureePreferee;

    public static Finder<Long, Proposal> find = new Finder<Long, Proposal>(Long.class, Proposal.class);

    public static List<Proposal> findAllForDisplay() {
        return find.select("id, title,  dureePreferee, dureeApprouve, statusProposal, speaker.id, speaker.fullname, speaker.avatar")
                .fetch("speaker").fetch("dureePreferee").fetch("dureeApprouve").findList();
    }

    public static int findNbProposals(boolean draft) {
        return Ebean.createSqlQuery("select count(*) as c from proposal where draft = :draft ").setParameter("draft",draft).findUnique().getInteger("c");
    }

    public static int findNbProposalsAcceptes() {
        return Ebean.createSqlQuery("select count(*) as c from proposal t where t.status_proposal='A'").findUnique().getInteger("c");
    }

    public static int findNbProposalsRejetes() {
        return Ebean.createSqlQuery("select count(*) as c from proposal t where t.status_proposal='R'").findUnique().getInteger("c");
    }

    public static Proposal findByTitle(String title) {
        return find.where().eq("title", title).findUnique();
    }

    public static List<Proposal> findBySpeaker(User speaker) {
        return find.where().eq("speaker", speaker).findList();
    }

    public static List<Proposal> findByEvent(Event event) {
        return find.where().eq("event", event).findList();
    }

    public static List<Proposal> findBySpeakerAndStatus(User speaker, StatusProposal status) {
        return find.where().eq("statusProposal", status.getInterne()).eq("speaker", speaker).findList();
    }

    public static List<Proposal> findByStatus(StatusProposal status) {
        return find.where().eq("statusProposal", status.getInterne()).findList();
    }

    public static List<Proposal> findByNoStatus() {
        return find.where().isNull("statusProposal").findList();
    }

    public static List<Proposal> findByStatusForMinimalData(StatusProposal status) {
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
                .where().eq("statusProposal", status.getInterne()).findList();
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
}
