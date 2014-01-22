package models;

import models.utils.BooleanUtils;
import models.utils.Mail;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    public Proposal proposal;

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
    	Set<String> emails = new HashSet<String>();
        if (BooleanUtils.isNotTrue(privateComment)) {
    	    addMailIfNotAuthorAndWantReceive(proposal.speaker, emails);
            for (User coSpeaker : proposal.getCoSpeakers()) {
                addMailIfNotAuthorAndWantReceive(coSpeaker, emails);
            }
        }
    	for (User admin : User.findAllAdmin()) {
    		addMailIfNotAuthorAndWantReceive(admin, emails);
    	}

        if (!emails.isEmpty()) {
            String urlString = "http://" + Configuration.root().getString("server.hostname");
            urlString += "/#/proposals/see/" + proposal.id;
            URL url = new URL(urlString);

            String subjet = Messages.get("proposals.comment.new.mail.subject", proposal.title);
            String message = Messages.get("proposals.comment.new.mail.message", proposal.title, author.getFullname(), comment,url);

            Mail.sendMail(new Mail.Envelop(subjet, message, emails));
        }
    }
    
    private void addMailIfNotAuthorAndWantReceive(User contact, Set<String> emails) {
		if (isNotAuthor(contact)
				&& wantReceive(contact)) {
			emails.add(contact.email);
		}
	}

	private boolean wantReceive(User contact) {
		return isSpeakerOfProposalAndWantReceive(contact)
				|| isAdminAndWantReceiveAll(contact) || isAdminAndHasCommentAndWantReceive(contact);
	}

	private boolean isAdminAndHasCommentAndWantReceive(User contact) {
		return contact.admin && contact.getNotifAdminOnProposalWithComment()
				&& getAuthorsOfComments().contains(contact.id);
	}

	private boolean isAdminAndWantReceiveAll(User contact) {
		return contact.admin && contact.getNotifAdminOnAllProposal();
	}

	private boolean isSpeakerOfProposalAndWantReceive(User contact) {
		return contact.equals(proposal.speaker) && contact.getNotifOnMyProposal();
	}

	private boolean isNotAuthor(User contact) {
		return !contact.equals(author);
	}
    
    private Set<Long> authorsOfComments = null;
    
    private Set<Long> getAuthorsOfComments() {
    	if (authorsOfComments == null) {
	    	authorsOfComments = new HashSet<Long>();
	    	for (Comment otherComment : proposal.comments) {
	    		authorsOfComments.add(otherComment.author.id);
	    	}
		}
		return authorsOfComments;
	}

    public static List<Comment> findByAuthor(User author) {
        return find.where().eq("author", author).findList();
    }

}
