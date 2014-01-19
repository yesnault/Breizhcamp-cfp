package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("serial")
@Entity
public class Link extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(50)
    @Column(length = 50)
    public String label;

    @Constraints.Required
    @Formats.NonEmpty
    @Constraints.MaxLength(200)
    @Column(length = 200)
    public String url;

    @Constraints.Required
    @JsonIgnore
    public LinkType linkType = LinkType.OTHER;

    @JsonProperty("type")
    public String getType() {
        return linkType.name();
    }

    @JsonProperty("icon")
    public String getIcon() {
        return linkType.getIcon();
    }

    @JsonProperty("preUrl")
    public String getPreUrl() {
        return linkType.getUrl();
    }

    public static Model.Finder<Long, Link> find = new Model.Finder<Long, Link>(Long.class, Link.class);

}
