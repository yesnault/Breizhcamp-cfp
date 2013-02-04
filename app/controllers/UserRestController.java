package controllers;

import static play.libs.Json.toJson;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

/**
 * Controlleur pour gérer les requêtes Ajax liées aux utilisateurs
 * 
 * @author lhuet
 */
@SecureSocial.SecuredAction(ajaxCall = true)
public class UserRestController extends Controller {

    private static User getLoggedUser() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.findByExternalId(socialUser.id().id(), socialUser.id().providerId());
        return user;
    }

    /**
     * Récupère l'ensemble des utilisateurs
     *
     * @return Liste des utilisateurs en JSON
     */
    public static Result get() {
        User user = getLoggedUser();
        // Requête réservée aux admins
        if (!user.admin) {
            return forbidden();
        }
        return ok(toJson(User.findAll()));
    }

    /**
     * Récupère un utilisateur par son id
     *
     * @param id
     * @return Objet utilisateur en JSON
     */
    public static Result getUser(Long id) {
        // TODO: Vérifier getUser(id) n'est pas réservé à un admin => Pb de secu !
        User user = User.findById(id);
        if (user == null) {
            return notFound();
        }

        return ok(toJson(user));
    }

    /**
     * Récupère les données de l'utilisateur loggué
     * 
     * @return Objet Utilisateur en JSON
     */
    public static Result getUserLogged() {
        User user = getLoggedUser();
        return ok(toJson(user));
    }
}
