package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Proposal proposal;

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

    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }


    public static Model.Finder<Long, Vote> find = new Model.Finder<Long, Vote>(Long.class, Vote.class);

    public static Vote findVoteByUserAndProposal(User user, Proposal proposal) {
        return find.query().where().eq("user", user).eq("proposal", proposal).findUnique();
    }

    public static Map<Long, Vote> findVotesUserByProposalId(User user) {
        
        List<Vote> listeVotes = find.query().fetch("proposal").where().eq("user", user).findList();
        Map<Long, Vote> votes = new HashMap<Long, Vote>();
        for (Vote vote : listeVotes) {
            votes.put(vote.proposal.getId(), vote);
        }
        return votes;
    }
    
    public static int findNbVotesUser(User user) {
        String sql = "select count(*) as c from vote v where v.user_id='" + user.id + "'";
        return Ebean.createSqlQuery(sql).findUnique().getInteger("c");
    }

    // Retour d'une chaine formatée "moyenne;nbvote"
    public static Map<Long, Pair<Double, Integer>> caculMoyennes() {
        String sql = "SELECT v.proposal_id as proposalId, avg(v.note) as moy, count(v.proposal_id) as nbVote, t.title as proposalTitle FROM vote v, proposal t where (v.proposal_id=t.id) group by v.proposal_id order by moy desc";
        List<SqlRow> rows = Ebean.createSqlQuery(sql).findList();
        
        Map<Long, Pair<Double, Integer>> moyennes = new HashMap<Long, Pair<Double, Integer>>();
        for (SqlRow row : rows) {
            Pair<Double, Integer> moyProposal = new ImmutablePair(row.getDouble("moy"), row.getInteger("nbVote"));
            moyennes.put(row.getLong("proposalId"), moyProposal);
        }
        
        return moyennes;
    }
    
    public static Double calculMoyenne(Proposal proposal) {
        Double moyenne = null;
        int sum = 0;
        int nbVotes = 0;
        for (Vote vote : find.query().where().eq("proposal", proposal).findList()) {
            sum = sum + vote.getNote();
            nbVotes++;
        }
        if (nbVotes > 0) {
            moyenne = ((double)sum) / ((double)nbVotes);
        }
        return moyenne;
    }
}
