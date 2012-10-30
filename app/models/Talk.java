package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class Talk extends Model {
	
	@Id
	public Long id;
	
	@Constraints.Required
	@Constraints.MaxLength(50)	
	@Formats.NonEmpty
	@Column(unique = true, length = 50)
	public String title;
	
	@Constraints.Required
	@Constraints.MaxLength(2000)
	@Formats.NonEmpty
	@Column(length = 2000)
	public String description;
	
	@ManyToOne
	public User speaker;
	
	@OneToMany(mappedBy ="talk")
	public List<Comment> comments;
	
	public List<Comment> getComments() {
		if (comments == null) {
			comments = new ArrayList<Comment>();
		}
		return comments;
	}

    public StatusTalk statusTalk;

	public static Finder<Long, Talk> find = new Finder<Long, Talk>(Long.class, Talk.class);
	
	
	public static Talk findByTitle(String title) {
		return find.where().eq("title", title).findUnique();
	}
	
	public static List<Talk> findBySpeaker(User speaker) {
		return find.where().eq("speaker", speaker).findList();
	}
}
