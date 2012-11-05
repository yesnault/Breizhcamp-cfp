package models;

import play.Logger;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
public class VoteStatus extends Model {

    @Id
    public Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VoteStatusEnum status;

    private static Model.Finder<Long, VoteStatus> find = new Model.Finder<Long, VoteStatus>(Long.class, VoteStatus.class);


    public static VoteStatusEnum getVoteStatus() {
        VoteStatus currentStatus = find.findUnique();
        if (currentStatus == null) {
            return VoteStatusEnum.NOT_BEGIN;
        }
        return currentStatus.status;
    }
    private static void deleteAllVotesStatus() {
        for (VoteStatus status : find.all()) {
            status.delete();
        }
    }

    public synchronized static void changeVoteStatus(VoteStatusEnum newStatus) {
        deleteAllVotesStatus();
        VoteStatus currentStatus = new VoteStatus();
        currentStatus.status = newStatus;
        currentStatus.save();
    }
}
