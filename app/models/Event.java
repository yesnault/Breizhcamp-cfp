package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Event extends Model {

    @Id
    private Long id;

    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    @Column(unique = true, length = 50)
    private String name;

    @Constraints.Required
    @Constraints.MaxLength(5)
    @Formats.NonEmpty
    @Column(unique = true, length = 5)
    private String shortName;

    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(200)
    @Column(length = 200)
    private String url;

    @Constraints.Required
    @Constraints.MaxLength(1000)
    @Formats.NonEmpty
    @Column(length = 1000)
    private String description;

    @Constraints.MaxLength(1000)
    @Column(length = 1000)
    private String cgu;

    @OneToOne
    private Agenda agenda;

    @ManyToMany(mappedBy = "events")
    @JsonIgnore
    private List<User> organizers = new ArrayList<User>();


    public static Finder<Long, Event> find = new Finder<Long, Event>(Long.class, Event.class);

    public static Event findByName(String name) {
        return find.query().where().eq("name", name).findUnique();
    }

    public static Event findByUrl(String url) {
        Event event = find.query().where().eq("url", url).findUnique();
        return event;
    }

    public List<User> getOrganizers() {
        if (organizers == null) {
            organizers = new ArrayList<User>();
        }
        return organizers;
    }

    public static Event getDefaut() {
        return getDefaut("");
    }

    public static Event getDefaut(String url) {
        Event event = Event.findByUrl(url);
        if (event == null) {
            event = new Event();
            event.setUrl(url);
            event.shortName = "DEF";
            event.setName("Evénement par défaut");
            event.save();
        } else {
            event.update();
        }
        return event;
    }

    public Agenda getAgenda() {
        return agenda;
    }

    public void setAgenda(Agenda agenda) {
        this.agenda = agenda;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCgu() {
        return cgu;
    }

    public void setCgu(String cgu) {
        this.cgu = cgu;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
