package models;

import models.utils.AppException;
import models.utils.Hash;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.BooleanUtils;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: yesnault
 * Date: 20/01/12
 */
@SuppressWarnings("serial")
@Entity
public class User extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @Formats.NonEmpty
    @Column(unique = true)
    public String email;

    @Constraints.Required
    @Formats.NonEmpty
    @Column(unique = true)
    public String fullname;

    public String confirmationToken;

    @Constraints.Required
    @Formats.NonEmpty
    public String passwordHash;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date dateCreation;

    @Formats.NonEmpty
    public Boolean validated = false;

	public Boolean admin = false;

    private Boolean notifOnMyTalk;

    private Boolean notifAdminOnAllTalk;

    private Boolean notifAdminOnTalkWithComment;

    @Constraints.Pattern("^([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])$")
    public String adresseMac;

    @OneToMany(mappedBy = "user")
    private List<DynamicFieldValue> dynamicFieldValues;

    public List<DynamicFieldValue> getDynamicFieldValues() {
        if (dynamicFieldValues == null) {
            dynamicFieldValues = new ArrayList<DynamicFieldValue>();
        }
        return dynamicFieldValues;
    }

    public boolean getNotifOnMyTalk() {
		return BooleanUtils.isNotFalse(notifOnMyTalk);
	}
	
	public boolean getNotifAdminOnAllTalk() {
		return BooleanUtils.isNotFalse(notifAdminOnAllTalk);
	}
	
	public boolean getNotifAdminOnTalkWithComment() {
		return BooleanUtils.isNotFalse(notifAdminOnTalkWithComment);
	}
	
	public void setNotifOnMyTalk(Boolean notifOnMyTalk) {
		this.notifOnMyTalk = notifOnMyTalk;
	}

	public void setNotifAdminOnAllTalk(Boolean notifAdminOnAllTalk) {
		this.notifAdminOnAllTalk = notifAdminOnAllTalk;
	}

	public void setNotifAdminOnTalkWithComment(Boolean notifAdminOnTalkWithComment) {
		this.notifAdminOnTalkWithComment = notifAdminOnTalkWithComment;
	}

	@Column(length = 2000)
	public String description;
	
	@OneToMany( cascade = CascadeType.ALL)
	public List<Lien> liens;
	
	public List<Lien> getLiens() {
		if (liens == null) {
			liens = new ArrayList<Lien>();
		}
		return liens;
	}
	
	
	private transient String avatar; 

	private final static String GRAVATAR_URL = "http://www.gravatar.com/avatar/";
	
	public String getAvatar() {
		if (avatar == null) {
			String emailHash = DigestUtils.md5Hex(email.toLowerCase().trim());
			avatar = GRAVATAR_URL + emailHash + ".jpg";
		}
		return avatar;
	}

    // -- Queries (long id, user.class)
    public static Model.Finder<Long, User> find = new Model.Finder<Long, User>(Long.class, User.class);

    /**
     * Retrieve a user from an email.
     *
     * @param email email to search
     * @return a user
     */
    public static User findByEmail(String email) {
        return find.where().eq("email", email).findUnique();
    }

    /**
     * Retrieve a user from a fullname.
     *
     * @param fullname Full name
     * @return a user
     */
    public static User findByFullname(String fullname) {
        return find.where().eq("fullname", fullname).findUnique();
    }

    /**
     * Retrieves a user from a confirmation token.
     *
     * @param token the confirmation token to use.
     * @return a user if the confirmation token is found, null otherwise.
     */
    public static User findByConfirmationToken(String token) {
        return find.where().eq("confirmationToken", token).findUnique();
    }

	public static List<User> findAll() {
		return find.all();
	}

    /**
     * Authenticate a User, from a email and clear password.
     *
     * @param email         email
     * @param clearPassword clear password
     * @return User if authenticated, null otherwise
     * @throws AppException App Exception
     */
    public static User authenticate(String email, String clearPassword) throws AppException {

        // get the user with email only to keep the salt password
        User user = find.where().eq("email", email).findUnique();
        if (user != null) {
            // get the hash password from the salt + clear password
            if (Hash.checkPassword(clearPassword, user.passwordHash)) {
              return user;
            }
        }
        return null;
    }

    public void changePassword(String password) throws AppException {
        this.passwordHash = Hash.createPassword(password);
        this.save();
    }
    
    public static List<User> findAllAdmin() {
    	return find.where().eq("admin", Boolean.TRUE).findList();
    }

    /**
     * Confirms an account.
     *
     * @return true if confirmed, false otherwise.
     * @throws AppException App Exception
     */
    public static boolean confirm(User user) throws AppException {
        if (user == null) {
          return false;
        }

		// If there's no admin for now, the new confirm user is admin.
		if (find.where().eq("admin", Boolean.TRUE).findRowCount() == 0) {
			user.admin = true;
		}
        user.confirmationToken = null;
        user.validated = true;
        user.save();
        return true;
    }

}
