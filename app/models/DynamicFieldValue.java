package models;


import org.codehaus.jackson.annotate.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class DynamicFieldValue extends Model {

    @Id
    private Long id;

    private String value;

    @ManyToOne
    private DynamicField dynamicField;

    @ManyToOne
    @JsonIgnore
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DynamicField getDynamicField() {
        return dynamicField;
    }

    public void setDynamicField(DynamicField dynamicField) {
        this.dynamicField = dynamicField;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public static Model.Finder<Long, DynamicFieldValue> find = new Model.Finder<Long, DynamicFieldValue>(Long.class, DynamicFieldValue.class);
}
