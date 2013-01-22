package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class ExternalUserId extends Model {


	@Id
	public Long id;
	
	@ManyToOne
	public User user;
	
	public String providerUuid;
	
	public String providerId;
	
	public ExternalUserId(String extId, String providerId) {
		this.providerUuid = extId;
		this.providerId = providerId;
	}
}
