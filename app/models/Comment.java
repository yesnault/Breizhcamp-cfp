package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import models.utils.Mail;

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
    public Talk talk;

    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(140)
    @Column(length = 140)
    public String comment;
    

    public static Model.Finder<Long, Comment> find = new Model.Finder<Long, Comment>(Long.class, Comment.class);
    
    public void sendMail() {
    	List<String> emails = new ArrayList<String>();
    	addMailIfNotAuthor(talk.speaker, emails);
    	for (User admin : User.findAllAdmin()) {
    		addMailIfNotAuthor(admin, emails);
    	}
    	
    	String subjet = Messages.get("talks.comment.new.mail.subject", talk.title);
    	String message = Messages.get("talks.comment.new.mail.message", talk.title, author.fullname, comment);
    	
    	Mail.sendMail(new Mail.Envelop(subjet, message, emails));
    	
    }
    
    private void addMailIfNotAuthor(User contact, List<String> emails) {
    	if (!contact.equals(author)) {
    		emails.add(contact.email);
    	}
    }

}
