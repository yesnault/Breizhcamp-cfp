package models;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

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

    public static Finder<Long, Creneau> find = new Finder<Long, Creneau>(Long.class, Creneau.class);

    public static Creneau findByLibelle(String libelle) {
        return find.query().where().eq("libelle", libelle).findUnique();
    }
}
