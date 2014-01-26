package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Entity
public class TalkFormat extends Model {

    @Id
    private Long id;

    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    @Column(length = 50)
    private String libelle;

    @Constraints.Required
    private Integer dureeMinutes;

    private String description;

    private Integer nbInstance;

    @ManyToOne
    public Event event;

    @OneToMany
    @JsonIgnore
    private List<Proposal> proposals;

    public List<Proposal> getProposals() {
        if (proposals == null) {
            proposals = new ArrayList<Proposal>();
        }
        return proposals;
    }

    // Give a hashKey unique for angular.
    @JsonProperty("$$hashKey")
    public String getHashKey() {
        return "creneau_" + id;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
    public static Finder<Long, TalkFormat> find = new Finder<Long, TalkFormat>(Long.class, TalkFormat.class);

    public static TalkFormat findByLibelle(String libelle, Event event) {
        return find.query().where().eq("libelle", libelle).eq("event", event).findUnique();
    }

    public static List<TalkFormat> findByEvent(Event event) {
        return find.query().where().eq("event", event).findList();
    }

    public Integer getNbInstance() {
        return nbInstance;
    }

    public void setNbInstance(Integer nbInstance) {
        this.nbInstance = nbInstance;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
