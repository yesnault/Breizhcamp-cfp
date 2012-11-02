package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class VoteStatus extends Model {

    @Id
    public Long id;

    public VoteStatusEnum status;

    private static Model.Finder<Long, VoteStatus> find = new Model.Finder<Long, VoteStatus>(Long.class, VoteStatus.class);

    public static VoteStatusEnum getVoteStatus() {
        VoteStatus currentStatus = find.findUnique();
        if (currentStatus == null) {
            return VoteStatusEnum.NOT_BEGIN;
        }
        return currentStatus.status;
    }

    public synchronized static void changeVoteStatus(VoteStatusEnum newStatus) {
        VoteStatus currentStatus = find.findUnique();
        if (currentStatus == null) {
            currentStatus = new VoteStatus();
        }
        currentStatus.status = newStatus;
        currentStatus.save();
    }
}
