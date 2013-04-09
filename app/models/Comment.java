package models;

import models.utils.BooleanUtils;
import models.utils.Mail;
import org.codehaus.jackson.annotate.JsonIgnore;
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

    public boolean clos;

    public Boolean privateComment;

    @OneToMany(mappedBy = "question")
    public List<Comment> reponses;

    @ManyToOne
    @JsonIgnore
    public Comment question;
    

    public static Model.Finder<Long, Comment> find = new Model.Finder<Long, Comment>(Long.class, Comment.class);
    
    public void sendMail() throws MalformedURLException {
    	List<String> emails = new ArrayList<String>();
        if (BooleanUtils.isNotTrue(privateComment)) {
    	    addMailIfNotAuthorAndWantReceive(talk.speaker, emails);
        }
    	for (User admin : User.findAllAdmin()) {
    		addMailIfNotAuthorAndWantReceive(admin, emails);
    	}

        if (!emails.isEmpty()) {
            String urlString = "http://" + Configuration.root().getString("server.hostname");
            urlString += "/#/talks/see/" + talk.id;
            URL url = new URL(urlString);

            String subjet = Messages.get("talks.comment.new.mail.subject", talk.title);
            String message = Messages.get("talks.comment.new.mail.message", talk.title, author.fullname, comment,url);

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

    public static List<Comment> findByAuthor(User author) {
        return find.where().eq("author", author).findList();
    }

}
