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
public class Track extends Model {

    @Id
    private Long id;

    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    @Column(unique = true, length = 50)
    private String title;

    @Constraints.Required
    @Constraints.MaxLength(5)
    @Formats.NonEmpty
    @Column(unique = true, length = 5)
    private String shortTitle;

    @Constraints.Required
    @Constraints.MaxLength(1000)
    @Formats.NonEmpty
    @Column(length = 1000)
    private String description;


    @ManyToMany(mappedBy = "tracksReview")
    public List<User> reviewers;

    @ManyToMany(mappedBy = "tracksAdvice")
    public List<User> advisors;

    public static Model.Finder<Long, Track> find = new Model.Finder<Long, Track>(Long.class, Track.class);

    public static Track findByTitle(String title) {
        return find.query().where().eq("title", title).findUnique();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
