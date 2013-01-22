package service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.ExternalUserId;
import models.User;
import play.Application;
import play.Logger;
import scala.Option;
import securesocial.core.Identity;
import securesocial.core.UserId;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;

/**
 * Classe utilisée par SecureSocial pour la gestion des Identity
 *
 */
public class CfpUserService extends BaseUserService {

	// TODO Eventuellement mettre les données Identity et Token en base (contient les infos sur OAuth notamment)
	// Pour le CFP, le nombre de User ne devrait pas être très important => laisser en HashMap pour le moment
    private HashMap<String, Identity> users  = new HashMap<String, Identity>();
    private HashMap<String, Token> tokens = new HashMap<String, Token>();
    

    public CfpUserService(Application application) {
		super(application);
	}
	
	@Override
	public Identity doFind(UserId userId) {
		Logger.debug("doFind SecureSocial Find User by Id : " + userId.id());
        return users.get(userId.id() + userId.providerId());
	}
	
	@Override
	public void doSave(Identity socialUser) {

		// Sauvegarde dans une HashMap en mémoire de l'objet SocialUser
	    users.put(socialUser.id().id() + socialUser.id().providerId(), socialUser);
		
		// Recherche d'un user existant et création ou mise à jour des données en SGBD
	    User userCfp = User.findByExternalId(socialUser.id().id(), socialUser.id().providerId());
		if (userCfp == null) {
			Logger.debug("Création du user : " + socialUser.fullName());
			userCfp = new User();
			// Selon les providers externes (twitter par ex.), on ne récupère pas toujours le mail
			if (socialUser.email().isDefined()) 
				userCfp.email = socialUser.email().get(); 
			userCfp.admin = false;
			userCfp.fullname = socialUser.fullName();
			userCfp.dateCreation = new Date();
			userCfp.validated = true;
			if (socialUser.avatarUrl().isDefined())
				userCfp.avatar = socialUser.avatarUrl().get();
			if (userCfp.extUserIds == null || userCfp.extUserIds.isEmpty()) {
				Logger.debug("Création du userid : " + socialUser.id().id() + " / " + socialUser.id().providerId());
				ExternalUserId extUserId = new ExternalUserId(socialUser.id().id(), socialUser.id().providerId());
				List<ExternalUserId> extUserIds = new ArrayList<>();
				extUserIds.add(extUserId);
				userCfp.extUserIds = extUserIds;
			}
		} 
		else {
			Logger.debug("Mise à jour du user : " + socialUser.fullName());
			userCfp.fullname = socialUser.fullName();
			if (socialUser.avatarUrl().isDefined())
				userCfp.avatar = socialUser.avatarUrl().get();
		}
		userCfp.save();
	}
	
    /**
     * Deletes all expired tokens
     *
     * Note: If you do not plan to use the UsernamePassword provider just provide en empty
     * implementation
     *
     */
	@Override
	public void doDeleteExpiredTokens() {
		Logger.debug("doDeleteExpiredTokens SecureSocial");
	}

    /**
     * Deletes a token
     *
     * Note: If you do not plan to use the UsernamePassword provider just provide en empty
     * implementation
     *
     * @param uuid the token id
     */
	@Override
	public void doDeleteToken(String uuid) {
		Logger.debug("doDeleteToken SecureSocial");
	}

    /**
     * Finds an identity by email and provider id.
     *
     * Note: If you do not plan to use the UsernamePassword provider just provide en empty
     * implementation.
     *
     * @param email - the user email
     * @param providerId - the provider id
     * @return an Identity instance or null if no user matches the specified id
     */
	@Override
	public Identity doFindByEmailAndProvider(String email, String providerId) {
    	Logger.debug("doFindByEmailAndProvider SecureSocial : " + email + " / " + providerId);
        Identity result = null;
        for( Identity user : users.values() ) {
            Option<String> optionalEmail = user.email();
            if ( user.id().providerId().equals(providerId) &&
                 optionalEmail.isDefined() &&
                 optionalEmail.get().equalsIgnoreCase(email))
            {
                result = user;
                break;
            }
        }
        return result;
	}

    /**
     * Finds a token
     *
     * Note: If you do not plan to use the UsernamePassword provider just provide en empty
     * implementation
     *
     * @param tokenId the token id
     * @return a Token instance or null if no token matches the specified id
     */
	@Override
	public Token doFindToken(String tokenId) {
	   	Logger.debug("doFindToken SecureSocial");
        return tokens.get(tokenId);
	}


    /**
     * Saves a token
     *
     * Note: If you do not plan to use the UsernamePassword provider just provide en empty
     * implementation
     *
     * @param token
     */
	@Override
	public void doSave(Token token) {
        tokens.put(token.uuid, token);
        Logger.debug("doSave SecureSocial Token : " + token.getEmail());
	}

}
