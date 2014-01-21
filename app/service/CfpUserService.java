package service;

import java.util.*;

import models.Credentials;
import models.User;
import play.Application;
import play.Logger;
import play.i18n.Messages;
import scala.Option;
import securesocial.core.AuthenticationMethod;
import securesocial.core.Identity;
import securesocial.core.OAuth1Info;
import securesocial.core.OAuth2Info;
import securesocial.core.PasswordInfo;
import securesocial.core.SocialUser;
import securesocial.core.IdentityId;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;

import static play.libs.Json.toJson;

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
        Logger.debug("doFind SecureSocial Find User by Id : " + identityId.userId() + " / " + identityId.providerId());
        // Recherche d'un user existant et création ou mise à jour des données en SGBD
        User userCfp = User.findByExternalId(identityId.userId(), identityId.providerId());
        Identity identity = null;
        if (userCfp!=null) {
            identity = userToIdentity(userCfp);
        }
        return identity;
    }

    @Override
    public Identity doSave(Identity socialUser) {

        // Recherche d'un user existant et création ou mise à jour des données en SGBD
        if (socialUser.email().isEmpty()) {
            throw new IllegalArgumentException("OAuth authentication need to be configured with user's email scope");
        }
        User userCfp = User.findByEmail(socialUser.email().get());
        Logger.debug("doSave " + socialUser.fullName() + " / socialIdentityId : " + socialUser.identityId().userId() + " - " + socialUser.identityId().providerId());
        if (userCfp == null) {
            Logger.debug("Création du user : " + socialUser.fullName());
            userCfp = IdentityToUser(socialUser);

            if(userCfp.fullname == null || userCfp.fullname.equals("") ){
               // TODO renvoyer une erreur
            }


            userCfp.admin = false;
            userCfp.dateCreation = new Date();
        } else {
            Logger.debug("Mise à jour du user : " + socialUser.fullName());
            userCfp.fullname = socialUser.fullName();
            if (socialUser.avatarUrl().isDefined()) {
                userCfp.avatar = socialUser.avatarUrl().get();
            }

            if (socialUser.passwordInfo().isDefined()) {
                PasswordInfo pInfo = socialUser.passwordInfo().get();
                userCfp.credentials.passwordHasher = pInfo.hasher();
                userCfp.credentials.password = pInfo.password();
                if (pInfo.salt().isDefined()) {
                     userCfp.credentials.passwordSalt = pInfo.salt().get();
                }
            }
        }

        if(User.findAll().isEmpty()){
            userCfp.admin = true;
        }

        userCfp.save();
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
        Logger.debug("doDeleteExpiredTokens SecureSocial");
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
        Logger.debug("doDeleteToken SecureSocial : " + uuid);
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
        Logger.debug("doFindByEmailAndProvider SecureSocial : " + email + " / " + providerId);
        Identity result = null;
        User user = User.findByEmail(email);
        if (user != null) {
            result = userToIdentity(user);
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
        Logger.debug("doFindToken SecureSocial : " + tokenId);
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
        Logger.debug("doSave SecureSocial Token : " + token.getEmail() + " / " + token.getUuid());
    }

    /**
     * Convertit un objet Identity en objet User (modèle)
     *
     * @param socialUser
     * @return
     */
    private User IdentityToUser(Identity socialUser) {

        User user = new User();

        user.fullname = socialUser.fullName();
        if (socialUser.avatarUrl().isDefined()) {
            user.avatar = socialUser.avatarUrl().get();
        }
        if (socialUser.email().isDefined()) {
            user.email = socialUser.email().get();
        }
        user.authenticationMethod = socialUser.authMethod().method();

        user.credentials = new Credentials();
        user.credentials.extUserId = socialUser.identityId().userId();
        user.credentials.providerId = socialUser.identityId().providerId();
        user.credentials.firstName = socialUser.firstName();
        user.credentials.lastName = socialUser.lastName();

        if (socialUser.passwordInfo().isDefined()) {
            PasswordInfo pInfo = socialUser.passwordInfo().get();
            user.credentials.passwordHasher = pInfo.hasher();
            user.credentials.password = pInfo.password();
            if (pInfo.salt().isDefined()) {
                user.credentials.passwordSalt = pInfo.salt().get();
            }
        }

        if (socialUser.oAuth1Info().isDefined()) {
            OAuth1Info oAuth1 = socialUser.oAuth1Info().get();
            user.credentials.oAuth1Secret = oAuth1.secret();
            user.credentials.oAuth1Token = oAuth1.token();
        }

        if (socialUser.oAuth2Info().isDefined()) {
            OAuth2Info oAuth2 = socialUser.oAuth2Info().get();
            user.credentials.oAuth2AccessToken = oAuth2.accessToken();
            if (oAuth2.expiresIn().isDefined()) {
                user.credentials.oAuth2ExpiresIn = (Integer) oAuth2.expiresIn().get();
            }
            if (oAuth2.tokenType().isDefined()) {
                user.credentials.oAuth2TokenType = oAuth2.tokenType().get();
            }
            if (oAuth2.refreshToken().isDefined()) {
                user.credentials.oAuth2RefreshToken = oAuth2.refreshToken().get();
            }
        }

        return user;
    }

    /**
     * Convertit un objet User (modèle) en objet SocialUser/Identity
     *
     * @param user
     * @return
     */
    private Identity userToIdentity(User user) {

        IdentityId identityId;
        String firstName = null;
        String lastName = null;
        if (user.authenticationMethod.equals("userPassword")) {
            identityId = new IdentityId(user.email, "userPassword");
        } else {
            identityId = new IdentityId(user.credentials.extUserId, user.credentials.providerId);
            firstName = user.credentials.firstName;
            lastName = user.credentials.lastName;
        }

        OAuth1Info oAuth1 = null;
        OAuth2Info oAuth2 = null;
        PasswordInfo passwordInfo = null;
        if (user.authenticationMethod.equals("oauth1")) {
            oAuth1 = new OAuth1Info(user.credentials.oAuth1Token,
                    user.credentials.oAuth1Secret);
        } else if (user.authenticationMethod.equals("oauth2")) {
            oAuth2 = new OAuth2Info(user.credentials.oAuth2AccessToken,
                    Option.apply(user.credentials.oAuth2TokenType),
                    Option.apply((Object) user.credentials.oAuth2ExpiresIn),
                    Option.apply(user.credentials.oAuth2RefreshToken));
        } else if (user.authenticationMethod.equals("userPassword")) {
            passwordInfo = new PasswordInfo(user.credentials.passwordHasher,
                    user.credentials.password,
                    Option.apply(user.credentials.passwordSalt));
        }

        SocialUser socialUser = new SocialUser(identityId,
                firstName,
                lastName,
                user.fullname,
                Option.apply(user.email),
                Option.apply(user.avatar),
                new AuthenticationMethod(user.authenticationMethod),
                Option.apply(oAuth1),
                Option.apply(oAuth2),
                Option.apply(passwordInfo));
        return (Identity) socialUser;
    }
}
