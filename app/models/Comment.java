package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import models.utils.Mail;

import org.apache.commons.lang.BooleanUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;

@SuppressWarnings("serial")
@Entity
public class Comment extends Model {

    @Id
    public Long id;

    @ManyToOne
    public User author;
    
    @ManyToOne
    @JsonIgnore
    public Talk talk;

    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(140)
    @Column(length = 140)
    public String comment;

    public Boolean privateComment;
    

    public static Model.Finder<Long, Comment> find = new Model.Finder<Long, Comment>(Long.class, Comment.class);
    
    public void sendMail() {
    	List<String> emails = new ArrayList<String>();
        if (BooleanUtils.isNotTrue(privateComment)) {
    	    addMailIfNotAuthorAndWantReceive(talk.speaker, emails);
        }
    	for (User admin : User.findAllAdmin()) {
    		addMailIfNotAuthorAndWantReceive(admin, emails);
    	}

        if (!emails.isEmpty()) {
            String subjet = Messages.get("talks.comment.new.mail.subject", talk.title);
            String message = Messages.get("talks.comment.new.mail.message", talk.title, author.fullname, comment);

            Mail.sendMail(new Mail.Envelop(subjet, message, emails));
        }
    }
    
    private void addMailIfNotAuthorAndWantReceive(User contact, List<String> emails) {
		if (isNotAuthor(contact)
				&& wantReceive(contact)) {
			emails.add(contact.email);
		}
	}

	private boolean wantReceive(User contact) {
		return isSpeakerOfTalkAndWantReceive(contact)
				|| isAdminAndWantReceiveAll(contact) || isAdminAndHasCommentAndWantReceive(contact);
	}

	private boolean isAdminAndHasCommentAndWantReceive(User contact) {
		return contact.admin && contact.getNotifAdminOnTalkWithComment()
				&& getAuthorsOfComments().contains(contact.id);
	}

	private boolean isAdminAndWantReceiveAll(User contact) {
		return contact.admin && contact.getNotifAdminOnAllTalk();
	}

	private boolean isSpeakerOfTalkAndWantReceive(User contact) {
		return contact.equals(talk.speaker) && contact.getNotifOnMyTalk();
	}

	private boolean isNotAuthor(User contact) {
		return !contact.equals(author);
	}
    
    private Set<Long> authorsOfComments = null;
    
    private Set<Long> getAuthorsOfComments() {
    	if (authorsOfComments == null) {
	    	authorsOfComments = new HashSet<Long>();
	    	for (Comment otherComment : talk.comments) {
	    		authorsOfComments.add(otherComment.author.id);
	    	}
		}
		return authorsOfComments;
	}

}
