package models;

import org.codehaus.jackson.annotate.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Vote extends Model {

    @Id
    private Long id;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToOne
    @JsonIgnore
    private Talk talk;

    private Integer note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Talk getTalk() {
        return talk;
    }

    public void setTalk(Talk talk) {
        this.talk = talk;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }


    public static Model.Finder<Long, Vote> find = new Model.Finder<Long, Vote>(Long.class, Vote.class);

    public static Vote findVoteByUserAndTalk(User user, Talk talk) {
        return find.query().where().eq("user", user).eq("talk", talk).findUnique();
    }

    public static Double calculMoyenne(Talk talk) {
        Double moyenne = null;
        int sum = 0;
        int nbVotes = 0;
        for (Vote vote : find.query().where().eq("talk", talk).findList()) {
            sum = sum + vote.getNote();
            nbVotes++;
        }
        if (nbVotes > 0) {
            moyenne = ((double)sum) / ((double)nbVotes);
        }
        return moyenne;
    }
}
