package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class Link extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(50)
    @Column(length = 50)
    public String label;

    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(200)
    @Column(length = 200)
    public String url;

    public static Model.Finder<Long, Link> find = new Model.Finder<Long, Link>(Long.class, Link.class);

}
