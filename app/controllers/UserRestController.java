package controllers;

import static play.libs.Json.toJson;

import models.User;
import models.utils.TransformValidationErrors;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlleur pour gérer les requêtes Ajax liées aux utilisateurs
 *
 * @author lhuet
 */
@SecureSocial.SecuredAction(ajaxCall = true)
public class UserRestController extends BaseController {

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

        List<User> users = new ArrayList<User>();
        for (User userJson : User.findAll()) {
            User userOut = new User();
            userOut.id = userJson.id;
            userOut.fullName = userJson.fullName;
            userOut.email =  userJson.email;
            userOut.admin  =  userJson.admin;
            userOut.credentials =  userJson.credentials;
            users.add(userOut);
        }

        return ok(toJson(users));
    }

    public static Result getCoSpeakers() {
        User user = getLoggedUser();

        List<User> coSpeakers = new ArrayList<User>();
        for (User coSpeaker : User.findAll()) {
            if (!coSpeaker.id.equals(user.id)) {
                User coSpeakerOut = new User();
                coSpeakerOut.id = coSpeaker.id;
                coSpeakerOut.fullName = coSpeaker.fullName;
                coSpeakers.add(coSpeakerOut);
            }
        }
        return ok(toJson(coSpeakers));
    }

    /**
     * Récupère un utilisateur par son id
     *
     * @param id
     * @return Objet utilisateur en JSON
     */
    public static Result getUser(Long id) {
        User user = User.findById(id);
        if (user == null) {
            return notFound();
        }
        User userLogged = getLoggedUser();

        if (!userLogged.admin && !user.id.equals(userLogged.id)) {
            // Si on est pas admin ou le speaker, on filtre les infos retournées.
            user.filterInfos();
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
