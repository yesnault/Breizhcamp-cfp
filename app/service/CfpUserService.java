package service;

import java.util.*;

import models.Credentials;
import models.User;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;
import play.Application;
import play.Logger;
import scala.Option;
import securesocial.core.Identity;
import securesocial.core.OAuth1Info;
import securesocial.core.OAuth2Info;
import securesocial.core.PasswordInfo;
import securesocial.core.SocialUser;
import securesocial.core.IdentityId;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;

/**
 * Classe utilisée par SecureSocial pour la gestion des Identity
 *
 */
public class CfpUserService extends BaseUserService {

    // TODO Eventuellement mettre les données Token en base (contient les hash pour créer les comptes locaux)
    // Pour le CFP, le nombre de User ne devrait pas être très important => laisser en HashMap pour le moment
//    private HashMap<String, Identity> users = new HashMap<String, Identity>();
    private HashMap<String, Token> tokens = new HashMap<String, Token>();

    public CfpUserService(Application application) {
        super(application);
    }

    @Override
    public Identity doFind(IdentityId identityId) {
        Logger.info("doFind SecureSocial Find User by Id : " + identityId.userId() + " / " + identityId.providerId());
        // Recherche d'un user existant et création ou mise à jour des données en SGBD
        User userCfp = User.findByExternalId(identityId.userId(), identityId.providerId());
        Identity identity = null;
        if (userCfp!=null) {
            identity = userToIdentity(userCfp, identityId);
        }
        Logger.info("doFind result : " + identityId.userId() + " / " + identityId.providerId() + " :: " + identity);
        return identity;
    }

    @Override
    public Identity doSave(Identity socialUser) {

        Logger.info("doSave " + socialUser.fullName() + " / socialIdentityId : " + socialUser.identityId().userId() + " - " + socialUser.identityId().providerId());
        // Recherche d'un user existant et création ou mise à jour des données en SGBD
        if (socialUser.email().isEmpty()) {
            throw new IllegalArgumentException("OAuth authentication need to be configured with user's email scope");
        }
        User userCfp = User.findByEmail(socialUser.email().get());
        if (userCfp == null) {
            Logger.info("Création du user : " + socialUser.fullName());
            userCfp = new User();

            userCfp.admin = false;
            userCfp.dateCreation = new Date();
        } else {
            Logger.info("Mise à jour du user : " + socialUser.fullName());
        }
        populateUser(userCfp, socialUser);

        return socialUser;
    }

    /**
     * Deletes all expired tokens
     *
     * Note: If you do not plan to use the UsernamePassword provider just
     * provide en empty implementation
     *
     */
    @Override
    public void doDeleteExpiredTokens() {
        Logger.info("doDeleteExpiredTokens SecureSocial");
    }

    /**
     * Deletes a token
     *
     * Note: If you do not plan to use the UsernamePassword provider just
     * provide en empty implementation
     *
     * @param uuid the token id
     */
    @Override
    public void doDeleteToken(String uuid) {
        Logger.info("doDeleteToken SecureSocial : " + uuid);
    }

    /**
     * Finds an identity by email and provider id.
     *
     * Note: If you do not plan to use the UsernamePassword provider just
     * provide en empty implementation.
     *
     * @param email - the user email
     * @param providerId - the provider id
     * @return an Identity instance or null if no user matches the specified id
     */
    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
        Logger.info("doFindByEmailAndProvider SecureSocial : " + email + " / " + providerId);
        Identity result = null;
        User user = User.findByEmail(email);
        if (user != null) {
            result = userToIdentity(user, providerId);
        }
        return result;
    }

    /**
     * Finds a token
     *
     * Note: If you do not plan to use the UsernamePassword provider just
     * provide en empty implementation
     *
     * @param tokenId the token id
     * @return a Token instance or null if no token matches the specified id
     */
    @Override
    public Token doFindToken(String tokenId) {
        Logger.info("doFindToken SecureSocial : " + tokenId);
        return tokens.get(tokenId);
    }

    /**
     * Saves a token
     *
     * Note: If you do not plan to use the UsernamePassword provider just
     * provide en empty implementation
     *
     * @param token
     */
    @Override
    public void doSave(Token token) {
        tokens.put(token.uuid, token);
        Logger.info("doSave SecureSocial Token : " + token.getEmail() + " / " + token.getUuid());
    }

    /**
     * Convertit un objet Identity en objet User (modèle)
     *
     * @param socialUser
     * @return
     */
    private User populateUser(User user, Identity socialUser)  {

        if (user.email == null && socialUser.email().isDefined())
            user.email = socialUser.email().get();
        if (user.fullName == null)
            user.fullName = socialUser.fullName();
        if (user.avatar == null && user.email != null) {
            user.avatar = "https://www.gravatar.com/avatar/" + DigestUtils.md5Hex(user.email.getBytes());;
            if (socialUser.avatarUrl().isDefined()) {
                try {
                    user.avatar += "?d=" + new URLCodec().encode(socialUser.avatarUrl().get());
                } catch (EncoderException e) {
                    // ??
                }
            }
        }

        // First user to login on a fresh new instance (so, DEV instance) is automatically set as admin
        if (User.findAll().isEmpty()){
            user.admin = true;
        }

        user.save();

        String provider = socialUser.identityId().providerId();
        boolean known = false;
        for (Credentials credential : user.credentials) {
            if (credential.providerId.equals(provider)) {
                known = false;
            }
        }

        if (!known) {
            Credentials credentials = new Credentials();
            credentials.extUserId = socialUser.identityId().userId();
            credentials.providerId = socialUser.identityId().providerId();

            if (socialUser.passwordInfo().isDefined()) {
                PasswordInfo pInfo = socialUser.passwordInfo().get();
                credentials.passwordHasher = pInfo.hasher();
                credentials.password = pInfo.password();
                if (pInfo.salt().isDefined()) {
                    credentials.passwordSalt = pInfo.salt().get();
                }
            }

            if (socialUser.oAuth1Info().isDefined()) {
                OAuth1Info oAuth1 = socialUser.oAuth1Info().get();
                credentials.oAuth1Secret = oAuth1.secret();
                credentials.oAuth1Token = oAuth1.token();
            }

            if (socialUser.oAuth2Info().isDefined()) {
                OAuth2Info oAuth2 = socialUser.oAuth2Info().get();
                credentials.oAuth2AccessToken = oAuth2.accessToken();
                if (oAuth2.expiresIn().isDefined()) {
                    credentials.oAuth2ExpiresIn = (Integer) oAuth2.expiresIn().get();
                }
                if (oAuth2.tokenType().isDefined()) {
                    credentials.oAuth2TokenType = oAuth2.tokenType().get();
                }
                if (oAuth2.refreshToken().isDefined()) {
                    credentials.oAuth2RefreshToken = oAuth2.refreshToken().get();
                }
            }
            credentials.user = user;
            credentials.save();
        }
        return user;
    }

    private Identity userToIdentity(User user, IdentityId id) {
        return userToIdentity(user, id.providerId());
    }

    /**
     * Convertit un objet User (modèle) en objet SocialUser/Identity
     *
     * @param user
     * @return
     */
    private Identity userToIdentity(User user, String providerId) {
        for (Credentials credential : user.credentials) {
            if (credential.providerId.equals(providerId)) {
                SocialUser socialUser = new SocialUser(
                        new IdentityId(credential.extUserId, credential.providerId),
                        null,
                        null,
                        user.fullName,
                        Option.apply(user.email),
                        Option.apply(user.avatar),
                        null,
                        Option.apply(new OAuth1Info(
                                credential.oAuth1Token,
                                credential.oAuth1Secret)),
                        Option.apply(new OAuth2Info(
                                credential.oAuth2AccessToken,
                                Option.apply(credential.oAuth2TokenType),
                                Option.<Object>apply(credential.oAuth2ExpiresIn),
                                Option.apply(credential.oAuth2RefreshToken))),
                        Option.apply(new PasswordInfo(
                                credential.passwordHasher,
                                credential.password,
                                Option.apply(credential.passwordSalt))));
                return socialUser;
            }
        }

        return null;
    }

}
