package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class Credentials extends Model {

    @Id
    public Long id;

    @ManyToOne
    public User user;

    public String extUserId;
    public String providerId;

    public String oAuth1Token;
    public String oAuth1Secret;
    
    public String oAuth2AccessToken;
    public String oAuth2TokenType;
    public Integer oAuth2ExpiresIn;
    public String oAuth2RefreshToken;
    
    public String passwordHasher;
    public String password;
    public String passwordSalt;

}
