package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.utils.BooleanUtils;
import org.apache.commons.codec.digest.DigestUtils;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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
    public String fullName;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    public List<Credentials> credentials;

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
    public List<Link> links;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<DynamicFieldValue> dynamicFieldValues;

    @ManyToMany
    @JsonIgnore
    private List<Proposal> coSpeakedProposals;

    @ManyToMany
    @JsonIgnore
    private List<Track> tracksReview;

    @ManyToMany
    @JsonIgnore
    private List<Track> tracksAdvice;

    @ManyToMany
    @JsonIgnore
    private List<Event> events;


    @JsonIgnore
    public String avatar;
    private final static String GRAVATAR_URL = "http://www.gravatar.com/avatar/";


    public List<Event> getEvents() {
        if (events == null) {
            events = new ArrayList<Event>();
        }
        return events;
    }

    public boolean hasEvent(Event event) {
        for(Event eventL : events){
         if(eventL.getId() == event.getId()){
             return true;
         }
        }
        return false;
    }

    @JsonProperty("events")
    public ArrayNode getEventsName() {
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);

        for (Event event : events) {
            ObjectNode eventJson = Json.newObject();
            eventJson.put("id", event.getId());
            eventJson.put("shortName", event.getShortName());
            result.add(eventJson);
        }

        return result;
    }

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

    @JsonProperty("isInfoValid")
    public boolean isInfoValid() {
        if (isEmpty(email) || isEmpty(fullName)) {
            return false;
        }
        if (!admin && isEmpty(description)) {
            return false;
        }

        return true;
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

    public List<Link> getLinks() {
        if (links == null) {
            links = new ArrayList<Link>();
        }
        return links;
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

    public static List<User> findAll() {
        return find.all();
    }


    public static List<User> findAllAdmin() {
        return find.where().eq("admin", Boolean.TRUE).findList();
    }

    public void filterInfos() {
        adresseMac = null;
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

        if (!email.equals(user.email)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }

    public String getFullname() {
        return fullName;
    }



}