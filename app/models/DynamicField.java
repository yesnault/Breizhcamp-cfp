package models;


import org.codehaus.jackson.annotate.JsonIgnore;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DynamicField extends Model {

    @Id
    private Long id;

    @Column(unique = true, length = 50)
    @Constraints.Required
    @Constraints.MaxLength(50)
    @Formats.NonEmpty
    private String name;

    @OneToMany(mappedBy = "dynamicField")
    @JsonIgnore
    private List<DynamicFieldValue> dynamicFieldValues;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DynamicFieldValue> getDynamicFieldValues() {
        if (dynamicFieldValues == null) {
            dynamicFieldValues = new ArrayList<DynamicFieldValue>();
        }
        return dynamicFieldValues;
    }

    public static Model.Finder<Long, DynamicField> find = new Model.Finder<Long, DynamicField>(Long.class, DynamicField.class);

    public static DynamicField findByName(String name) {
        return find.query().where().eq("name", name).findUnique();
    }
}
