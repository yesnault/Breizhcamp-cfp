package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("serial")
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

    public static Map<Long, Vote> findVotesUserByTalkId(User user) {
        
        List<Vote> listeVotes = find.query().fetch("talk").where().eq("user", user).findList();
        Map<Long, Vote> votes = new HashMap<Long, Vote>();
        for (Vote vote : listeVotes) {
            votes.put(vote.talk.id, vote);
        }
        return votes;
    }
    
    public static int findNbVotesUser(User user) {
        String sql = "select count(*) as c from vote v where v.user_id='" + user.id + "'";
        return Ebean.createSqlQuery(sql).findUnique().getInteger("c");
    }

    // Retour d'une chaine formatée "moyenne;nbvote"
    public static Map<Long, Pair<Double, Integer>> caculMoyennes() {
        String sql = "SELECT v.talk_id as talkId, avg(v.note) as moy, count(v.talk_id) as nbVote, t.title as talkTitle FROM vote v, talk t where (v.talk_id=t.id) group by v.talk_id order by moy desc";
        List<SqlRow> rows = Ebean.createSqlQuery(sql).findList();
        
        Map<Long, Pair<Double, Integer>> moyennes = new HashMap<>();
        for (SqlRow row : rows) {
            Pair<Double, Integer> moyTalk = new ImmutablePair(row.getDouble("moy"), row.getInteger("nbVote"));
            moyennes.put(row.getLong("talkId"), moyTalk);
        }
        
        return moyennes;
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
