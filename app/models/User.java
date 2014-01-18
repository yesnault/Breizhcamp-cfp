package models;

import org.apache.commons.codec.digest.DigestUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;
import models.utils.BooleanUtils;

/**
 *
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

    /**
     * Valeur de l'objet secureSocial
     * oauth1 / oauth2 / userPassword / openId
     */
    @JsonIgnore
    public String authenticationMethod;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    public Credentials credentials;
    
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date dateCreation;

    @Formats.NonEmpty
    public Boolean admin = false;
    
    private Boolean notifOnMyProposal;
    private Boolean notifAdminOnAllProposal;
    private Boolean notifAdminOnProposalWithComment;
 
    @Constraints.Pattern("^([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])$")
    public String adresseMac;

    @Column(length = 2000)
    public String description;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Lien> liens;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<DynamicFieldValue> dynamicFieldValues;

    @ManyToMany
    @JsonIgnore
    private List<Proposal> coSpeakedProposals;

    @JsonIgnore
    public String avatar;
    private final static String GRAVATAR_URL = "http://www.gravatar.com/avatar/";
    

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
    
    
    @JsonProperty("provider")
    public String getProvider(){
        String provider = null;
        if (credentials!=null) {
            provider = this.credentials.providerId;
        }
        return provider;
    }

    public boolean getNotifOnMyProposal() {
        return BooleanUtils.isNotFalse(notifOnMyProposal);
    }

    public boolean getNotifAdminOnAllProposal() {
        return BooleanUtils.isNotFalse(notifAdminOnAllProposal);
    }

    public boolean getNotifAdminOnProposalWithComment() {
        return BooleanUtils.isNotFalse(notifAdminOnProposalWithComment);
    }

    public void setNotifOnMyProposal(Boolean notifOnMyProposal) {
        this.notifOnMyProposal = notifOnMyProposal;
    }

    public void setNotifAdminOnAllProposal(Boolean notifAdminOnAllProposal) {
        this.notifAdminOnAllProposal = notifAdminOnAllProposal;
    }

    public void setNotifAdminOnProposalWithComment(Boolean notifAdminOnProposalWithComment) {
        this.notifAdminOnProposalWithComment = notifAdminOnProposalWithComment;
    }

    public List<Lien> getLiens() {
        if (liens == null) {
            liens = new ArrayList<Lien>();
        }
        return liens;
    }

    @JsonProperty("avatar")
    public String getAvatar() {
        if (avatar == null && email != null) {
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
     * Retrieve a user from an Id.
     *
     * @param id id to search
     * @return a user
     */
    public static User findById(Long id) {
        return find.where().eq("id", id).findUnique();
    }

    /**
     * Retrieve a user from an external Id (SocialUser id/providerId).
     *
     * @param uuid uuid to search
     * @return a user
     */
    public static User findByExternalId(String userId, String providerId) {

        // Bug de SecureSocial ? socialUser.id().providerId() renvoie parfois userPassword au lieu de userpass
        if (providerId.equals("userPassword")) providerId = "userpass";
        
        return find.fetch("credentials").where()
                    .eq("credentials.extUserId", userId)
                    .eq("credentials.providerId", providerId)
                    .findUnique();
    }

    
    public static User findByEmailAndProvider(String email, String provider) {
        
        return find.fetch("credentials").where()
                    .eq("credentials.providerId", provider)
                    .eq("email", email)
                    .findUnique();
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

    public static List<User> findAll() {
        return find.all();
    }

    public static List<User> findAllAdmin() {
        return find.where().eq("admin", Boolean.TRUE).findList();
    }

    public void filterInfos() {
        adresseMac = null;
        authenticationMethod = null;
        admin = null;
        dateCreation = null;
        email = null;
        description = null;
        setNotifAdminOnAllProposal(null);
        setNotifAdminOnProposalWithComment(null);
        setNotifOnMyProposal(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        User user = (User) o;

        if (admin != null ? !admin.equals(user.admin) : user.admin != null) return false;
        if (adresseMac != null ? !adresseMac.equals(user.adresseMac) : user.adresseMac != null) return false;
        if (authenticationMethod != null ? !authenticationMethod.equals(user.authenticationMethod) : user.authenticationMethod != null)
            return false;
        if (avatar != null ? !avatar.equals(user.avatar) : user.avatar != null) return false;
        if (coSpeakedProposals != null ? !coSpeakedProposals.equals(user.coSpeakedProposals) : user.coSpeakedProposals != null)
            return false;
        if (credentials != null ? !credentials.equals(user.credentials) : user.credentials != null) return false;
        if (dateCreation != null ? !dateCreation.equals(user.dateCreation) : user.dateCreation != null) return false;
        if (description != null ? !description.equals(user.description) : user.description != null) return false;
        if (dynamicFieldValues != null ? !dynamicFieldValues.equals(user.dynamicFieldValues) : user.dynamicFieldValues != null)
            return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (fullname != null ? !fullname.equals(user.fullname) : user.fullname != null) return false;
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (liens != null ? !liens.equals(user.liens) : user.liens != null) return false;
        if (notifAdminOnAllProposal != null ? !notifAdminOnAllProposal.equals(user.notifAdminOnAllProposal) : user.notifAdminOnAllProposal != null)
            return false;
        if (notifAdminOnProposalWithComment != null ? !notifAdminOnProposalWithComment.equals(user.notifAdminOnProposalWithComment) : user.notifAdminOnProposalWithComment != null)
            return false;
        if (notifOnMyProposal != null ? !notifOnMyProposal.equals(user.notifOnMyProposal) : user.notifOnMyProposal != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}