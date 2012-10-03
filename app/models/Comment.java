package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

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

}
