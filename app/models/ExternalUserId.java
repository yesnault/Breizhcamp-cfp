package models;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class ExternalUserId extends Model {

	@Id
	public Long id;
	
	@OneToOne
	public User user;
	
	public String providerUuid;
	
	public String providerId;
	
	public ExternalUserId(String extId, String providerId) {
		this.providerUuid = extId;
		this.providerId = providerId;
	}
}
