package models;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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
    @Constraints.MaxLength(1000)
    @Formats.NonEmpty
    @Column(length = 1000)
    private String description;

    private boolean clos;


    public static Finder<Long, Event> find = new Finder<Long, Event>(Long.class, Event.class);

    public static Event findByName(String name) {
        return find.query().where().eq("name", name).findUnique();
    }

    public static Event findActif() {
        return findActif(false);
    }

    public static Event findActif(boolean edit) {
        Event event = find.query().where().eq("clos", false).findUnique();

        if (event == null && !edit) {
            if (Event.findByName("Evénement par défaut") == null) {
                event = new Event();
                event.setClos(false);
                event.setName("Evénement par défaut");
                event.save();
            } else {
                event = Event.findByName("Evénement par défaut");
                event.setClos(false);
                event.update();
            }
        }

        return event;
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

    public boolean isClos() {
        return clos;
    }

    public void setClos(boolean clos) {
        this.clos = clos;
    }


}
