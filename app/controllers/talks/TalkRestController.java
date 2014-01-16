package controllers.talks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.ybonnel.csvengine.CsvEngine;
import fr.ybonnel.csvengine.adapter.AdapterDouble;
import fr.ybonnel.csvengine.annotation.CsvColumn;
import fr.ybonnel.csvengine.annotation.CsvFile;
import models.*;
import models.utils.TransformValidationErrors;
import org.apache.commons.lang3.tuple.Pair;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.*;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class TalkRestController extends Controller {

    public static User getLoggedUser() {
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.findByExternalId(socialUser.identityId().userId(), socialUser.identityId().providerId());
        return user;
    }

    public static Result getById(Long idTalk) {
        Talk talk = Talk.find.byId(idTalk);

        User user = getLoggedUser();

        if (!user.admin && !user.id.equals(talk.speaker.id)) {
            // On vérifie que le user est admin où le propriétaire du talk
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (user.admin) {
            talk.vote = Vote.findVoteByUserAndTalk(user, talk);
        }
        talk.fiteredComments(user);
        talk.fiteredCoSpeakers();
        return ok(toJson(talk));
    }

    public static Result submitTalk(Long idTalk) {
        Talk talk = Talk.find.byId(idTalk);

        User user = getLoggedUser();

        if (!user.id.equals(talk.speaker.id) && !talk.getCoSpeakers().contains(user) ) {
            // On vérifie que le user est admin où le propriétaire du talk
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        talk.draft = false;
        talk.update();

        return ok(toJson(talk));
    }

    public static Result get() {
        User user = getLoggedUser();

        List<Talk> talks = Talk.findBySpeaker(user);

        for (Talk talk : talks) {
            if (user.admin) {
                talk.vote = Vote.findVoteByUserAndTalk(user, talk);
                if (VoteStatus.getVoteStatus() == VoteStatusEnum.CLOSED) {
                    talk.moyenne = Vote.calculMoyenne(talk);
                }
            }

            talk.fiteredComments(user);
            talk.fiteredCoSpeakers();
        }
        return ok(toJson(talks));
    }

    public static Result getTalks(Long userId) {
        User user = User.find.byId(userId);


        List<Talk> talks = Talk.findBySpeaker(user);
        for (Talk talk : talks) {
            talk.fiteredComments(user);
            talk.fiteredCoSpeakers();
            talk.filtereSpeaker();
        }
        return ok(toJson(talks));
    }

    public static Result getTalksByStatus(Long userId, String status) {
        StatusTalk statusTalk = StatusTalk.fromCode(status);
        User user = User.find.byId(userId);
        List<Talk> talks = Talk.findBySpeakerAndStatus(user, statusTalk);
        for (Talk talk : talks) {
            talk.fiteredComments(user);
            talk.fiteredCoSpeakers();
        }
        return ok(toJson(talks));
    }


    public static Result all() {
        return all(false);
    }

    public static Result all(boolean draft) {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        List<Talk> talks = Talk.findAllForDisplay();

        Map<Long, Vote> votes = Vote.findVotesUserByTalkId(user);

        Map<Long, Pair<Double, Integer>> moyennes = Vote.caculMoyennes();

        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);

        for (Talk talk : talks) {
            if (talk.draft == draft) {

                ObjectNode talkJson = Json.newObject();

                talkJson.put("id", talk.id);
                talkJson.put("title", talk.title);

                ArrayNode creneaux = new ArrayNode(JsonNodeFactory.instance);
                for (Creneau creneau : talk.getCreneaux()) {
                    ObjectNode creneauJson = Json.newObject();
                    creneauJson.put("id", creneau.getId());
                    creneauJson.put("libelle", creneau.getLibelle());
                    creneauJson.put("dureeMinutes", creneau.getDureeMinutes());
                    creneaux.add(creneauJson);
                }
                talkJson.put("creneaux", creneaux);
                if (talk.dureePreferee != null) {
                    talkJson.put("dureePreferee", talk.dureePreferee.getId());
                } else {
                    talkJson.putNull("dureePreferee");
                }
                if (talk.dureeApprouve != null) {
                    talkJson.put("dureeApprouve", talk.dureeApprouve.getId());
                } else {
                    talkJson.putNull("dureeApprouve");
                }
                if (talk.statusTalk != null) {
                    talkJson.put("statusTalk", talk.statusTalk.name());
                } else {
                    talkJson.putNull("statusTalk");
                }

                if (talk.speaker != null) {
                    ObjectNode speakerJson = Json.newObject();
                    speakerJson.put("id", talk.speaker.id);
                    speakerJson.put("fullname", talk.speaker.fullname);
                    speakerJson.put("avatar", talk.speaker.getAvatar());
                    talkJson.put("speaker", speakerJson);
                }

                Vote voteUser = votes.get(talk.id);
                if (voteUser != null) {
                    talkJson.put("vote", voteUser.getNote());
                } else {
                    talkJson.putNull("vote");
                }

                Pair<Double, Integer> moyenne = moyennes.get(talk.id);
                if (moyenne != null) {
                    talkJson.put("moyenne", moyenne.getLeft());
                    talkJson.put("nbvote", moyenne.getRight());
                } else {
                    talkJson.putNull("moyenne");
                }

                result.add(talkJson);
            }
        }

        return ok(result);
    }


    public static Result save() {
        User user = getLoggedUser();
        Form<Talk> talkForm = form(Talk.class).bindFromRequest();

        if (talkForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(talkForm.errors())));
        }

        Talk formTalk = talkForm.get();

        if (formTalk.id == null) {

            if (VoteStatus.getVoteStatus() != VoteStatusEnum.NOT_BEGIN) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.vote.begin"))));
            }
            // Nouveau talk
            formTalk.speaker = user;
            if (Talk.findByTitle(formTalk.title) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.talk.already.exist"))));
            }
            if (formTalk.getCreneaux() == null || formTalk.getCreneaux().isEmpty()) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.talk.creneaux.empty"))));
            }
            formTalk.draft = true;
            formTalk.save();
            formTalk.saveManyToManyAssociations("creneaux");
            List<User> coSpeakersInDb = new ArrayList<User>();
            for (User coSpeaker : formTalk.getCoSpeakers()) {
                coSpeakersInDb.add(User.findById(coSpeaker.id));
            }
            formTalk.getCoSpeakers().clear();
            formTalk.getCoSpeakers().addAll(coSpeakersInDb);
            formTalk.saveManyToManyAssociations("coSpeakers");
            formTalk.update();
            updateTags(talkForm.data().get("tagsname"), formTalk);
        } else {
            // Mise à jour d'un talk
            Talk dbTalk = Talk.find.byId(formTalk.id);

            if (!(user.id.equals(dbTalk.speaker.id) || user.admin)) {
                // On vérifie que le user est admin où le propriétaire du talk
                Logger.info("Tentative de suppression de talk sans les droits requis : " + dbTalk.id);
                return unauthorized();
            }


            if (!formTalk.title.equals(dbTalk.title)
                    && Talk.findByTitle(formTalk.title) != null) {
                Logger.error("error.talk.already.exist :" + formTalk.title);
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.talk.already.exist"))));
            }
            dbTalk.title = formTalk.title;
            dbTalk.description = formTalk.description;
            dbTalk.draft = true;
            dbTalk.save();
            updateCoSpeakers(formTalk, dbTalk);
            updateCreneaux(formTalk, dbTalk);
            updateTags(talkForm.data().get("tagsname"), dbTalk);
        }


        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    private static void updateCreneaux(Talk formTalk, Talk dbTalk) {
        Set<Long> creneauxInForm = new HashSet<Long>();
        for (Creneau creneau : formTalk.getCreneaux()) {
            creneauxInForm.add(creneau.getId());
        }
        List<Creneau> creneauxTmp = new ArrayList<Creneau>(dbTalk.getCreneaux());
        Set<Long> creneauxInDb = new HashSet<Long>();
        for (Creneau creneau : creneauxTmp) {
            if (!creneauxInForm.contains(creneau.getId())) {
                dbTalk.getCreneaux().remove(creneau);
            } else {
                creneauxInDb.add(creneau.getId());
            }
        }

        // ajout des creneaux ajoutés dans la liste
        for (Long idCreneau : creneauxInForm) {
            if (!creneauxInDb.contains(idCreneau)) {
                dbTalk.getCreneaux().add(Creneau.find.byId(idCreneau));
            }
        }
        dbTalk.saveManyToManyAssociations("creneaux");

        dbTalk.dureePreferee = formTalk.dureePreferee;
        //formTalk.dureePreferee.talksPrefere.add(dbTalk);
        //formTalk.dureePreferee.save();

        dbTalk.update();
    }

    private static void updateCoSpeakers(Talk formTalk, Talk dbTalk) {
        Set<Long> coSpeakersInForm = new HashSet<Long>();
        for (User coSpeaker : formTalk.getCoSpeakers()) {
            coSpeakersInForm.add(coSpeaker.id);
        }
        List<User> coSpeakersTmp = new ArrayList<User>(dbTalk.getCoSpeakers());
        Set<Long> coSpeakersInDb = new HashSet<Long>();
        for (User coSpeaker : coSpeakersTmp) {
            if (!coSpeakersInForm.contains(coSpeaker.id)) {
                dbTalk.getCoSpeakers().remove(coSpeaker);
            } else {
                coSpeakersInDb.add(coSpeaker.id);
            }
        }

        for (Long idCoSpeaker : coSpeakersInForm) {
            if (!coSpeakersInDb.contains(idCoSpeaker)) {
                dbTalk.getCoSpeakers().add(User.findById(idCoSpeaker));
            }
        }
        dbTalk.saveManyToManyAssociations("coSpeakers");
    }

    public static void updateTags(String tags, Talk dbTalk) {
        if (tags == null || tags.length() == 0) {
            return;
        }
        List<String> tagsList = Arrays.asList(tags.split(","));

        // suppression qui ne sont plus présent dans la nouvelle liste
        List<Tag> tagtmp = new ArrayList<Tag>(dbTalk.getTags());
        for (Tag tag : tagtmp) {
            if (!tagsList.contains(tag.nom)) {
                dbTalk.getTags().remove(tag);
            }
        }

        // ajout des tags ajoutés dans la liste
        for (String tag : tagsList) {
            if (!dbTalk.getTagsName().contains(tag)) {
                Tag dbTag = Tag.findByTagName(tag.toUpperCase());
                if (dbTag == null) {
                    dbTag = new Tag();
                    dbTag.nom = tag.toUpperCase();
                    dbTag.save();
                }
                Logger.debug("tags: = " + dbTag.id);
                dbTalk.getTags().add(dbTag);
            }
        }
        dbTalk.saveManyToManyAssociations("tags");
        dbTalk.update();
    }

    public static Result addTag(Long idTalk, String tags) {
        User user = getLoggedUser();
        Talk dbTalk = Talk.find.byId(idTalk);

        if (!user.admin && !user.id.equals(dbTalk.speaker.id)) {
            // On vérifie que le user est admin où le propriétaire du talk
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (dbTalk != null) {
            Logger.debug("addTags: = " + tags + " init tags " + dbTalk.getTagsName());
            updateTags(tags, dbTalk);
            Logger.debug("fin addTags: = " + dbTalk.getTagsName() + " size : " + dbTalk.getTags().size());
            return ok();
        } else {
            return notFound();
        }
    }

    public static Result delete(Long idTalk) {
        if (VoteStatus.getVoteStatus() != VoteStatusEnum.NOT_BEGIN) {
            return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.vote.begin"))));
        }

        Talk talk = Talk.find.byId(idTalk);

        User user = getLoggedUser();
        if (!user.admin && !(user.id.equals(talk.speaker.id))) {
            // On vérifie que le user est admin où le propriétaire du talk
            Logger.info("Tentative de suppression de talk sans les droits requis : " + talk.id);
            return unauthorized();
        }


        List<Comment> comments = new ArrayList<Comment>(talk.getComments());
        for (Comment comment : comments) {
            talk.getComments().remove(comment);
            for (Comment reponse : comment.reponses) {
                reponse.question = null;
                reponse.delete();
            }
            comment.update();
            comment.reponses = new ArrayList<Comment>();
            comment.delete();
        }

        List<Tag> tagtmp = new ArrayList<Tag>(talk.getTags());
        for (Tag tag : tagtmp) {
            talk.getTags().remove(tag);
        }
        talk.saveManyToManyAssociations("tags");

        List<Creneau> creneauxTmp = new ArrayList<Creneau>(talk.getCreneaux());
        for (Creneau creneau : creneauxTmp) {
            talk.getCreneaux().remove(creneau);
        }
        talk.saveManyToManyAssociations("creneaux");
        List<User> coSpeakersTmp = new ArrayList<User>(talk.getCoSpeakers());
        for (User coSpeaker : coSpeakersTmp) {
            talk.getCoSpeakers().remove(coSpeaker);
        }
        talk.saveManyToManyAssociations("coSpeakers");
        talk.delete();
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    public static Result saveComment(Long idTalk) throws MalformedURLException {
        User user = getLoggedUser();
        Talk talk = Talk.find.byId(idTalk);

        JsonNode node = request().body().asJson();
        String commentForm = null;
        boolean privateComment = false;
        if (node != null && node.get("comment") != null && !node.get("comment").equals("null")) {
            commentForm = node.get("comment").asText();
            if (user.admin && node.get("private") != null) {
                privateComment = node.get("private").asBoolean();
            }
        } else {
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("comment", Collections.singletonList(Messages.get("error.required")));
            return badRequest(toJson(errors));
        }

        if (!user.admin && !user.id.equals(talk.speaker.id)) {
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (commentForm.length() > 0 && commentForm.length() <= 140 && !commentForm.equals("null")) {
            Comment comment = new Comment();
            comment.author = user;
            comment.comment = commentForm;
            comment.privateComment = privateComment;
            comment.talk = talk;
            comment.save();
            comment.sendMail();
        }
        return ok();
    }

    public static Result closeComment(Long idTalk, Long idComment) {
        User user = getLoggedUser();
        Comment question = Comment.find.byId(idComment);

        if (!question.author.id.equals(user.id) && !user.admin) {
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("error", Collections.singletonList(Messages.get("error.close.comment.baduser")));
            return badRequest(toJson(errors));
        }

        question.clos = true;
        question.save();

        return ok();

    }

    public static Result deleteComment(Long idTalk, Long idComment) {
        User user = getLoggedUser();
        Comment question = Comment.find.byId(idComment);

        System.out.println(question.author.id);
        System.out.println(user.id);

        if (!question.author.id.equals(user.id) && !user.admin) {
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("error", Collections.singletonList(Messages.get("error.delete.comment.baduser")));
            return badRequest(toJson(errors));
        }

        if (question.reponses != null && question.reponses.size() > 0) {
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("error", Collections.singletonList(Messages.get("error.delete.comment")));
            return badRequest(toJson(errors));
        }

        question.delete();

        return ok();
    }

    public static Result saveReponse(Long idTalk, Long idComment) throws MalformedURLException {
        User user = getLoggedUser();
        Talk talk = Talk.find.byId(idTalk);
        Comment question = Comment.find.byId(idComment);

        JsonNode node = request().body().asJson();
        String commentForm = null;
        boolean privateComment = false;
        Logger.debug("nose : " + node.asText());
        if (node != null && node.get("comment") != null && !node.get("comment").equals("null")) {
            commentForm = node.get("comment").asText();
            if (user.admin && node.get("private") != null) {
                privateComment = node.get("private").asBoolean();
            }
        } else {
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("commentR", Collections.singletonList(Messages.get("error.required")));
            return badRequest(toJson(errors));
        }

        if (!user.admin && !user.id.equals(talk.speaker.id)) {
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (commentForm.length() > 0 && commentForm.length() <= 140 && !commentForm.equals("null")) {
            Comment comment = new Comment();
            comment.author = user;
            comment.comment = commentForm;
            comment.privateComment = privateComment;
            comment.talk = talk;


            if (question.reponses == null) {
                question.reponses = new ArrayList<Comment>();
            }

            comment.question = question;
            comment.save();

            question.reponses.add(comment);
            question.save();

            comment.sendMail();
        }
        return ok();
    }

    public static Result editComment(Long idTalk, Long idComment) throws MalformedURLException {
        User user = getLoggedUser();
        Talk talk = Talk.find.byId(idTalk);
        Comment question = Comment.find.byId(idComment);

        JsonNode node = request().body().asJson();
        String commentForm = null;
        Logger.debug("nose : " + node.asText());
        if (node != null && node.get("comment") != null && !node.get("comment").equals("null")) {
            commentForm = node.get("comment").asText();
        } else {
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("commentE", Collections.singletonList(Messages.get("error.required")));
            return badRequest(toJson(errors));
        }

        if (!user.admin && !user.id.equals(talk.speaker.id)) {
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (commentForm.length() > 0 && commentForm.length() <= 140 && !commentForm.equals("null")) {
            question.comment = commentForm;
            question.save();

            question.sendMail();
        }
        return ok();
    }

    public static Result saveStatus(Long idTalk) throws MalformedURLException {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Talk talk = Talk.find.byId(idTalk);

        JsonNode node = request().body().asJson();

        StatusTalk newStatus = StatusTalk.fromValue(node.get("status").asText());

        if (talk.statusTalk != newStatus) {
            talk.statusTalk = newStatus;

            if (talk.statusTalk.equals(StatusTalk.ACCEPTE)) {
                Creneau dureeApprouve = Creneau.find.byId(Long.valueOf(node.get("dureeApprouve").asText()));

                talk.dureeApprouve = dureeApprouve;
            } else {
                talk.dureeApprouve = null;
            }

            talk.save();
            if (talk.statusTalk != null) {
                talk.statusTalk.sendMail(talk, talk.speaker.email);
            }
        }

        return ok();
    }

    public static Result rejectAllTalkWithoutStatus() throws MalformedURLException {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        for (Talk talk : Talk.findByNoStatus()) {
            talk.statusTalk = StatusTalk.REJETE;
            talk.save();
            if (talk.speaker != null) {
                talk.statusTalk.sendMail(talk, talk.speaker.email);
            }
        }
        return ok();
    }

    public static Result saveVote(Long idTalk, Integer note) {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Talk talk = Talk.find.byId(idTalk);

        VoteStatusEnum voteStatus = VoteStatus.getVoteStatus();
        if (voteStatus != VoteStatusEnum.OPEN && voteStatus != VoteStatusEnum.NOT_BEGIN) {
            return unauthorized();
        }
        if (note == null || note < 1 || note > 5) {
            return badRequest();
        }

        Vote vote = Vote.findVoteByUserAndTalk(user, talk);
        if (vote == null) {
            vote = new Vote();
            vote.setUser(user);
            vote.setTalk(talk);
        }
        vote.setNote(note);
        vote.save();
        return ok();
    }

    public static Result talkStat() {

        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        ObjectNode result = Json.newObject();
        result.put("nbTalks", Talk.findNbTalks());
        result.put("nbAcceptes", Talk.findNbTalksAcceptes());
        result.put("nbRejetes", Talk.findNbTalksRejetes());
        result.put("nbVotesUser", Vote.findNbVotesUser(user));
        return ok(result);
    }


    @CsvFile(separator = ";")
    public static class TalkCsv {

        @CsvColumn(value = "speakerFullName", order = 1)
        public String speakerFullName;

        @CsvColumn(value = "title", order = 2)
        public String title;

        @CsvColumn(value = "status", order = 3)
        public String status;

        @CsvColumn(value = "moyenne", order = 4, adapter = AdapterDouble.class)
        public Double moyenne;

        @CsvColumn(value = "formatPrefere", order = 5)
        public String formatPrefere;

        @CsvColumn(value = "formats", order = 6)
        public String formats;

        @CsvColumn(value = "description", order = 7)
        public String description;


        public static TalkCsv fromTalk(Talk talk) {
            TalkCsv talkCsv = new TalkCsv();
            if (talk.speaker != null) {
                talkCsv.speakerFullName = talk.speaker.fullname;
                for (User coSpeaker : talk.getCoSpeakers()) {
                    talkCsv.speakerFullName += "\n" + coSpeaker.fullname;
                }
            }


            talkCsv.title = talk.title;
            if (talk.statusTalk != null) {
                talkCsv.status = talk.statusTalk.name();
            }

            if (VoteStatus.getVoteStatus() == VoteStatusEnum.CLOSED) {
                talkCsv.moyenne = Vote.calculMoyenne(talk);
            }

            if (talk.dureePreferee != null) {
                talkCsv.formatPrefere = talk.dureePreferee.getLibelle();
            }

            StringBuilder builderFormats = new StringBuilder();
            for (Creneau creneau : talk.getCreneaux()) {
                if (builderFormats.length() > 0) {
                    builderFormats.append('\n');
                }
                builderFormats.append(creneau.getLibelle());
            }
            talkCsv.formats = builderFormats.toString();

            talkCsv.description = talk.description;
            return talkCsv;
        }
    }

    public static Result getAllTalksInCsv() {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        List<TalkCsv> talks = new ArrayList<TalkCsv>();
        for (Talk talk : Talk.find.all()) {
            talks.add(TalkCsv.fromTalk(talk));
        }

        CsvEngine engine = new CsvEngine(TalkCsv.class);

        StringWriter writer = new StringWriter();

        engine.writeFile(writer, talks, TalkCsv.class);

        response().setContentType("application/octet-stream");
        response().setHeader("Content-Description", "File Transfer");
        response().setHeader("Content-Disposition", "attachment;filename=talks.csv");
        response().setHeader("Content-Transfer-Encoding", "binary");
        response().setHeader("Expires", "0");
        response().setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response().setHeader("Pragma", "public");
        return ok(writer.toString(), "ISO-8859-1");
    }
}
