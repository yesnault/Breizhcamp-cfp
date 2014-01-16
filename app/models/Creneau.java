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
public class Creneau extends Model {

    @Id
    private Long id;

    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    @Column(unique = true, length = 50)
    private String libelle;

    @Constraints.Required
    private Integer dureeMinutes;

    private String description;

    private Integer nbInstance;
    
    @OneToMany(mappedBy ="dureePreferee")
    @JsonIgnore
    public List<Talk> talksPrefere;

    @ManyToOne
    public Event event;

    @ManyToMany
    @JsonIgnore
    private List<Talk> talks;

    public List<Talk> getTalks() {
        if (talks == null) {
            talks = new ArrayList<Talk>();
        }
        return talks;
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
    
    
    public static Finder<Long, Creneau> find = new Finder<Long, Creneau>(Long.class, Creneau.class);

    public static Creneau findByLibelle(String libelle) {
        return find.query().where().eq("libelle", libelle).findUnique();
    }

    public Integer getNbInstance() {
        return nbInstance;
    }

    public void setNbInstance(Integer nbInstance) {
        this.nbInstance = nbInstance;
    }
}
