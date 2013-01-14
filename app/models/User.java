package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.BooleanUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

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
    public String fullname;

    //   [SocialUser(UserId(laurent.huet35@free.fr,userpass),
    //   Laurent,HUET,Laurent HUET,Some(laurent.huet35@free.fr),
    //   None,AuthenticationMethod(userPassword),None,None,
    //   Some(PasswordInfo(bcrypt,$2a$10$iH9snjDQsokeoSrJparn5OidMmRKdnyCVVZbawIiZlQ3p4aTHL6se,None)))]

    @Formats.NonEmpty
    public String providerId;
    
    @Formats.NonEmpty
    public String tokenUuid;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public DateTime tokenCreationTime;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public DateTime tokenModificationTime;

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
    @JsonIgnore
    private List<DynamicFieldValue> dynamicFieldValues;

    public List<DynamicFieldValue> getDynamicFieldValues() {
        if (dynamicFieldValues == null) {
            dynamicFieldValues = new ArrayList<DynamicFieldValue>();
        }
        return dynamicFieldValues;
    }

    @JsonProperty("dynamicFields")
    public List<DynamicFieldJson> getDynamicFieldsJson() {
        Map<Long, DynamicFieldValue> dynamicFieldValueByDynamicFieldId = new HashMap<Long, DynamicFieldValue>();
        for (DynamicFieldValue value : getDynamicFieldValues()) {
            dynamicFieldValueByDynamicFieldId.put(value.getDynamicField().getId(), value);
        }
        List<DynamicFieldJson> jsonFields = new ArrayList<DynamicFieldJson>();
        for (DynamicField field : DynamicField.find.all()) {
            jsonFields.add(DynamicFieldJson.toDynamicFieldJson(field, dynamicFieldValueByDynamicFieldId.get(field.getId())));
        }
        return jsonFields;
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

    @OneToMany(cascade = CascadeType.ALL)
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

    public static User findById(Long id) {
        return find.where().eq("id", id).findUnique();
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

    public static List<User> findAllAdmin() {
        return find.where().eq("admin", Boolean.TRUE).findList();
    }

}
