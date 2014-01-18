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
public class Talk extends Model {

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
    @ManyToMany(mappedBy = "coSpeakedTalks")
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
    @OneToMany(mappedBy = "talk")
    @JsonIgnore
    public List<Comment> comments;

    public List<Comment> getComments() {
        if (comments == null) {
            comments = new ArrayList<Comment>();
        }
        return comments;
    }
    public StatusTalk statusTalk;
    @ManyToMany(mappedBy = "talks")
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
    public Creneau dureePreferee;

    @ManyToOne
    public Creneau dureeApprouve;


    public static Finder<Long, Talk> find = new Finder<Long, Talk>(Long.class, Talk.class);

    public static List<Talk> findAllForDisplay() {
        return find.select("id, title,  dureePreferee, dureeApprouve, statusTalk, speaker.id, speaker.fullname, speaker.avatar")
                .fetch("speaker").fetch("dureePreferee").fetch("dureeApprouve").findList();
    }

    public static int findNbTalks(boolean draft) {
        return Ebean.createSqlQuery("select count(*) as c from talk where draft = :draft ").setParameter("draft",draft).findUnique().getInteger("c");
    }

    public static int findNbTalksAcceptes() {
        return Ebean.createSqlQuery("select count(*) as c from talk t where t.status_talk='A'").findUnique().getInteger("c");
    }

    public static int findNbTalksRejetes() {
        return Ebean.createSqlQuery("select count(*) as c from talk t where t.status_talk='R'").findUnique().getInteger("c");
    }

    public static Talk findByTitle(String title) {
        return find.where().eq("title", title).findUnique();
    }

    public static List<Talk> findBySpeaker(User speaker) {
        return find.where().eq("speaker", speaker).findList();
    }

    public static List<Talk> findByEvent(Event event) {
        return find.where().eq("event", event).findList();
    }

    public static List<Talk> findBySpeakerAndStatus(User speaker, StatusTalk status) {
        return find.where().eq("statusTalk", status.getInterne()).eq("speaker", speaker).findList();
    }

    public static List<Talk> findByStatus(StatusTalk status) {
        return find.where().eq("statusTalk", status.getInterne()).findList();
    }

    public static List<Talk> findByNoStatus() {
        return find.where().isNull("statusTalk").findList();
    }

    public static List<Talk> findByStatusForMinimalData(StatusTalk status) {
        // talk.id
        // talk.title
        // talk.description
        // talk.speaker.fullname
        // talk.speaker.avatar
        // talk.speaker.description
        // talk.speaker.liens.url
        // talk.speaker.liens.label
        // talk.coSpeakers
        return find.select("id, title, description, speaker.id, speaker.fullname, speaker.avatar, speaker.description, speaker.liens, " +
                "talk.coSpeakers.id, talk.coSpeakers.fullname, talk.coSpeakers.avatar, talk.coSpeakers.description, talk.coSpeakers.liens")
                .fetch("speaker").fetch("speaker.liens").fetch("coSpeakers").fetch("coSpeakers.liens")
                .where().eq("statusTalk", status.getInterne()).findList();
    }

    public static Talk findByIdWithFetch(Long id) {
        return find.select("id, title, description, speaker.id, speaker.fullname, speaker.avatar, speaker.description, speaker.liens, " +
                "talk.coSpeakers.id, talk.coSpeakers.fullname, talk.coSpeakers.avatar, talk.coSpeakers.description, talk.coSpeakers.liens, talks.tags.nom")
                .fetch("speaker").fetch("speaker.liens").fetch("coSpeakers").fetch("coSpeakers.liens").fetch("tags")
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
