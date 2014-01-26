package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Agenda  extends Model {

    @Id
    private Long id;

    private Date debutCfp;

    private Date finCfp;

    private Date finVote;

    public Long getId() {
        return id;
    }

    public Date getDebutCfp() {
        return debutCfp;
    }

    public void setDebutCfp(Date debutCfp) {
        this.debutCfp = debutCfp;
    }

    public Date getFinVote() {
        return finVote;
    }

    public void setFinVote(Date finVote) {
        this.finVote = finVote;
    }

    public Date getFinCfp() {
        return finCfp;
    }

    public void setFinCfp(Date finCfp) {
        this.finCfp = finCfp;
    }
}
