package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Track extends Model {

    @Id
    private Long id;

    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    @Column(length = 50)
    private String title;

    @Constraints.Required
    @Constraints.MaxLength(5)
    @Formats.NonEmpty
    @Column( length = 5)
    private String shortTitle;

    @Constraints.Required
    @Constraints.MaxLength(1000)
    @Formats.NonEmpty
    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "track",cascade = CascadeType.ALL)
    @JsonIgnore
    public List<Proposal> proposals;

    @ManyToMany(mappedBy = "tracksReview")
    public List<User> reviewers;

    @ManyToMany(mappedBy = "tracksAdvice")
    public List<User> advisors;

    @ManyToOne
    public Event event;

    public static Model.Finder<Long, Track> find = new Model.Finder<Long, Track>(Long.class, Track.class);

    public static Track findByTitleAndEvent(String title, Event event) {
        return find.query().where().eq("title", title).eq("event", event).findUnique();
    }

    public static Object findByShortTitleAndEvent(String shortTitle, Event event) {
        return find.query().where().eq("shortTitle", shortTitle).eq("event", event).findUnique();
    }

    public static List<Track> findByEvent(Event event) {
        return find.query().where().eq("event", event).findList();
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Proposal> getProposals() {
        if (proposals == null) {
            proposals = new ArrayList<Proposal>();
        }
        return proposals;
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
