package models;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;


@Entity
public class Tag extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @Formats.NonEmpty
    @Column(unique = true)
    public String nom;

    @ManyToMany
    public List<Talk> talks;

    public static Model.Finder<Long, Tag> find = new Model.Finder<Long, Tag>(Long.class, Tag.class);

    public static Tag findByTagName(String tag) {
        return find.where().eq("nom", tag).findUnique();
    }

    @Override
    public String toString() {
        return nom;
    }
}
