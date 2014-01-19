package controllers.proposals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.BaseController;
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
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.*;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class ProposalRestController extends BaseController {


    public static Result getById(Long idProposal) {
        Proposal proposal = Proposal.find.byId(idProposal);

        User user = getLoggedUser();

        if (!user.admin && !user.id.equals(proposal.speaker.id)) {
            // On vérifie que le user est admin où le propriétaire du proposal
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (user.admin) {
            proposal.vote = Vote.findVoteByUserAndProposal(user, proposal);
        }
        proposal.fiteredComments(user);
        proposal.fiteredCoSpeakers();
        return ok(toJson(proposal));
    }

    public static Result submitProposal(Long idProposal) {
        Proposal proposal = Proposal.find.byId(idProposal);

        User user = getLoggedUser();

        if (!user.id.equals(proposal.speaker.id) && !proposal.getCoSpeakers().contains(user)) {
            // On vérifie que le user est admin où le propriétaire du proposal
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        proposal.draft = false;
        proposal.update();

        return ok(toJson(proposal));
    }

    public static Result get() {
        User user = getLoggedUser();

        List<Proposal> proposals = Proposal.findBySpeaker(user);

        for (Proposal proposal : proposals) {
            if (user.admin) {
                proposal.vote = Vote.findVoteByUserAndProposal(user, proposal);
                if (VoteStatus.getVoteStatus() == VoteStatusEnum.CLOSED) {
                    proposal.moyenne = Vote.calculMoyenne(proposal);
                }
            }

            proposal.fiteredComments(user);
            proposal.fiteredCoSpeakers();
        }
        return ok(toJson(proposals));
    }

    public static Result getProposals(Long userId) {
        User user = User.find.byId(userId);


        List<Proposal> allProposals = Proposal.findBySpeaker(user);
        List<Proposal> proposals = new ArrayList<Proposal>();
        for (Proposal proposal : allProposals) {
            if (!proposal.draft) {
                proposal.fiteredComments(user);
                proposal.fiteredCoSpeakers();
                proposal.filtereSpeaker();

                proposals.add(proposal);
            }
        }
        return ok(toJson(proposals));
    }


    public static Result getProposalsDraft(Long userId) {
        User user = User.find.byId(userId);


        List<Proposal> allProposals = Proposal.findBySpeaker(user);
        List<Proposal> proposals = new ArrayList<Proposal>();
        for (Proposal proposal : allProposals) {
            if (proposal.draft) {
                proposal.fiteredComments(user);
                proposal.fiteredCoSpeakers();
                proposal.filtereSpeaker();

                proposals.add(proposal);
            }
        }
        return ok(toJson(proposals));
    }

    public static Result getProposalsByStatus(Long userId, String status) {
        StatusProposal statusProposal = StatusProposal.fromCode(status);
        User user = User.find.byId(userId);
        List<Proposal> proposals = Proposal.findBySpeakerAndStatus(user, statusProposal);
        for (Proposal proposal : proposals) {
            proposal.fiteredComments(user);
            proposal.fiteredCoSpeakers();
        }
        return ok(toJson(proposals));
    }


    public static Result all() {
        return all(false);
    }

    public static Result all(boolean draft) {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        List<Proposal> proposals = Proposal.findAllForDisplay();

        Map<Long, Vote> votes = Vote.findVotesUserByProposalId(user);

        Map<Long, Pair<Double, Integer>> moyennes = Vote.caculMoyennes();

        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);

        for (Proposal proposal : proposals) {
            if (proposal.draft == draft) {

                ObjectNode proposalJson = Json.newObject();

                proposalJson.put("id", proposal.id);
                proposalJson.put("title", proposal.title);


                if (proposal.dureePreferee != null) {
                    ObjectNode dureePrefereeJson = Json.newObject();
                    dureePrefereeJson.put("id", proposal.dureePreferee.getId());
                    dureePrefereeJson.put("dureeMinutes", proposal.dureePreferee.getDureeMinutes());
                    dureePrefereeJson.put("libelle", proposal.dureePreferee.getLibelle());
                    proposalJson.put("dureePreferee", dureePrefereeJson);
                } else {
                    proposalJson.putNull("dureePreferee");
                }
                if (proposal.dureeApprouve != null) {
                    proposalJson.put("dureeApprouve", proposal.dureeApprouve.getId());
                } else {
                    proposalJson.putNull("dureeApprouve");
                }
                if (proposal.statusProposal != null) {
                    proposalJson.put("statusProposal", proposal.statusProposal.name());
                } else {
                    proposalJson.putNull("statusProposal");
                }

                if (proposal.speaker != null) {
                    ObjectNode speakerJson = Json.newObject();
                    speakerJson.put("id", proposal.speaker.id);
                    speakerJson.put("fullname", proposal.speaker.fullname);
                    speakerJson.put("avatar", proposal.speaker.getAvatar());
                    proposalJson.put("speaker", speakerJson);
                }

                Vote voteUser = votes.get(proposal.id);
                if (voteUser != null) {
                    proposalJson.put("vote", voteUser.getNote());
                } else {
                    proposalJson.putNull("vote");
                }

                Pair<Double, Integer> moyenne = moyennes.get(proposal.id);
                if (moyenne != null) {
                    proposalJson.put("moyenne", moyenne.getLeft());
                    proposalJson.put("nbvote", moyenne.getRight());
                } else {
                    proposalJson.putNull("moyenne");
                }

                result.add(proposalJson);
            }
        }

        return ok(result);
    }


    public static Result save() {
        User user = getLoggedUser();
        Form<Proposal> proposalForm = form(Proposal.class).bindFromRequest();

        if (proposalForm.hasErrors()) {
            return badRequest(toJson(TransformValidationErrors.transform(proposalForm.errors())));
        }

        Proposal formProposal = proposalForm.get();

        if (formProposal.id == null) {

            if (VoteStatus.getVoteStatus() != VoteStatusEnum.NOT_BEGIN) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.vote.begin"))));
            }
            // Nouveau proposal
            formProposal.speaker = user;
            if (Proposal.findByTitle(formProposal.title) != null) {
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.proposal.already.exist"))));
            }

            formProposal.draft = true;
            formProposal.save();
            List<User> coSpeakersInDb = new ArrayList<User>();
            for (User coSpeaker : formProposal.getCoSpeakers()) {
                coSpeakersInDb.add(User.findById(coSpeaker.id));
            }
            formProposal.event = Event.findActif();
            formProposal.getCoSpeakers().clear();
            formProposal.getCoSpeakers().addAll(coSpeakersInDb);
            formProposal.saveManyToManyAssociations("coSpeakers");
            formProposal.update();
            updateTags(proposalForm.data().get("tagsname"), formProposal);
        } else {
            // Mise à jour d'un proposal
            Proposal dbProposal = Proposal.find.byId(formProposal.id);

            if (!(user.id.equals(dbProposal.speaker.id) || user.admin)) {
                // On vérifie que le user est admin où le propriétaire du proposal
                Logger.info("Tentative de suppression de proposal sans les droits requis : " + dbProposal.id);
                return unauthorized();
            }


            if (!formProposal.title.equals(dbProposal.title)
                    && Proposal.findByTitle(formProposal.title) != null) {
                Logger.error("error.proposal.already.exist :" + formProposal.title);
                return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.proposal.already.exist"))));
            }
            dbProposal.title = formProposal.title;
            dbProposal.description = formProposal.description;
            dbProposal.dureePreferee = formProposal.dureePreferee;
            dbProposal.indicationsOrganisateurs = formProposal.indicationsOrganisateurs;
            dbProposal.draft = true;
            dbProposal.save();
            updateCoSpeakers(formProposal, dbProposal);
            updateTags(proposalForm.data().get("tagsname"), dbProposal);
        }


        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }


    private static void updateCoSpeakers(Proposal formProposal, Proposal dbProposal) {
        Set<Long> coSpeakersInForm = new HashSet<Long>();
        for (User coSpeaker : formProposal.getCoSpeakers()) {
            coSpeakersInForm.add(coSpeaker.id);
        }
        List<User> coSpeakersTmp = new ArrayList<User>(dbProposal.getCoSpeakers());
        Set<Long> coSpeakersInDb = new HashSet<Long>();
        for (User coSpeaker : coSpeakersTmp) {
            if (!coSpeakersInForm.contains(coSpeaker.id)) {
                dbProposal.getCoSpeakers().remove(coSpeaker);
            } else {
                coSpeakersInDb.add(coSpeaker.id);
            }
        }

        for (Long idCoSpeaker : coSpeakersInForm) {
            if (!coSpeakersInDb.contains(idCoSpeaker)) {
                dbProposal.getCoSpeakers().add(User.findById(idCoSpeaker));
            }
        }
        dbProposal.saveManyToManyAssociations("coSpeakers");
    }

    public static void updateTags(String tags, Proposal dbProposal) {
        if (tags == null || tags.length() == 0) {
            return;
        }
        List<String> tagsList = Arrays.asList(tags.split(","));

        // suppression qui ne sont plus présent dans la nouvelle liste
        List<Tag> tagtmp = new ArrayList<Tag>(dbProposal.getTags());
        for (Tag tag : tagtmp) {
            if (!tagsList.contains(tag.nom)) {
                dbProposal.getTags().remove(tag);
            }
        }

        // ajout des tags ajoutés dans la liste
        for (String tag : tagsList) {
            if (!dbProposal.getTagsName().contains(tag)) {
                Tag dbTag = Tag.findByTagName(tag.toUpperCase());
                if (dbTag == null) {
                    dbTag = new Tag();
                    dbTag.nom = tag.toUpperCase();
                    dbTag.save();
                }
                Logger.debug("tags: = " + dbTag.id);
                dbProposal.getTags().add(dbTag);
            }
        }
        dbProposal.saveManyToManyAssociations("tags");
        dbProposal.update();
    }

    public static Result addTag(Long idProposal, String tags) {
        User user = getLoggedUser();
        Proposal dbProposal = Proposal.find.byId(idProposal);

        if (!user.admin && !user.id.equals(dbProposal.speaker.id)) {
            // On vérifie que le user est admin où le propriétaire du proposal
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (dbProposal != null) {
            Logger.debug("addTags: = " + tags + " init tags " + dbProposal.getTagsName());
            updateTags(tags, dbProposal);
            Logger.debug("fin addTags: = " + dbProposal.getTagsName() + " size : " + dbProposal.getTags().size());
            return ok();
        } else {
            return notFound();
        }
    }

    public static Result delete(Long idProposal) {
        if (VoteStatus.getVoteStatus() != VoteStatusEnum.NOT_BEGIN) {
            return badRequest(toJson(TransformValidationErrors.transform(Messages.get("error.vote.begin"))));
        }

        Proposal proposal = Proposal.find.byId(idProposal);

        User user = getLoggedUser();
        if (!user.admin && !(user.id.equals(proposal.speaker.id))) {
            // On vérifie que le user est admin où le propriétaire du proposal
            Logger.info("Tentative de suppression de proposal sans les droits requis : " + proposal.id);
            return unauthorized();
        }


        List<Comment> comments = new ArrayList<Comment>(proposal.getComments());
        for (Comment comment : comments) {
            proposal.getComments().remove(comment);
            for (Comment reponse : comment.reponses) {
                reponse.question = null;
                reponse.delete();
            }
            comment.update();
            comment.reponses = new ArrayList<Comment>();
            comment.delete();
        }

        List<Tag> tagtmp = new ArrayList<Tag>(proposal.getTags());
        for (Tag tag : tagtmp) {
            proposal.getTags().remove(tag);
        }
        proposal.saveManyToManyAssociations("tags");

        List<User> coSpeakersTmp = new ArrayList<User>(proposal.getCoSpeakers());
        for (User coSpeaker : coSpeakersTmp) {
            proposal.getCoSpeakers().remove(coSpeaker);
        }
        proposal.saveManyToManyAssociations("coSpeakers");
        proposal.delete();
        // HTTP 204 en cas de succès (NO CONTENT)
        return noContent();
    }

    public static Result saveComment(Long idProposal) throws MalformedURLException {
        User user = getLoggedUser();
        Proposal proposal = Proposal.find.byId(idProposal);

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

        if (!user.admin && !user.id.equals(proposal.speaker.id)) {
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (commentForm.length() > 0 && commentForm.length() <= 140 && !commentForm.equals("null")) {
            Comment comment = new Comment();
            comment.author = user;
            comment.comment = commentForm;
            comment.privateComment = privateComment;
            comment.proposal = proposal;
            comment.save();
            comment.sendMail();
        }
        return ok();
    }

    public static Result closeComment(Long idProposal, Long idComment) {
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

    public static Result deleteComment(Long idProposal, Long idComment) {
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

    public static Result saveReponse(Long idProposal, Long idComment) throws MalformedURLException {
        User user = getLoggedUser();
        Proposal proposal = Proposal.find.byId(idProposal);
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

        if (!user.admin && !user.id.equals(proposal.speaker.id)) {
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (commentForm.length() > 0 && commentForm.length() <= 140 && !commentForm.equals("null")) {
            Comment comment = new Comment();
            comment.author = user;
            comment.comment = commentForm;
            comment.privateComment = privateComment;
            comment.proposal = proposal;


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

    public static Result editComment(Long idProposal, Long idComment) throws MalformedURLException {
        User user = getLoggedUser();
        Proposal proposal = Proposal.find.byId(idProposal);
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

        if (!user.admin && !user.id.equals(proposal.speaker.id)) {
            return forbidden(toJson(TransformValidationErrors.transform("Action non autorisée")));
        }

        if (commentForm.length() > 0 && commentForm.length() <= 140 && !commentForm.equals("null")) {
            question.comment = commentForm;
            question.save();

            question.sendMail();
        }
        return ok();
    }

    public static Result saveStatus(Long idProposal) throws MalformedURLException {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Proposal proposal = Proposal.find.byId(idProposal);

        JsonNode node = request().body().asJson();

        StatusProposal newStatus = StatusProposal.fromValue(node.get("status").asText());

        if (proposal.statusProposal != newStatus) {
            proposal.statusProposal = newStatus;

            if (proposal.statusProposal.equals(StatusProposal.ACCEPTE)) {
                Creneau dureeApprouve = Creneau.find.byId(Long.valueOf(node.get("dureeApprouve").asText()));

                proposal.dureeApprouve = dureeApprouve;
            } else {
                proposal.dureeApprouve = null;
            }

            proposal.save();
            if (proposal.statusProposal != null) {
                proposal.statusProposal.sendMail(proposal, proposal.speaker.email);
            }
        }

        return ok();
    }

    public static Result rejectAllProposalWithoutStatus() throws MalformedURLException {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        for (Proposal proposal : Proposal.findByNoStatus()) {
            proposal.statusProposal = StatusProposal.REJETE;
            proposal.save();
            if (proposal.speaker != null) {
                proposal.statusProposal.sendMail(proposal, proposal.speaker.email);
            }
        }
        return ok();
    }

    public static Result saveVote(Long idProposal, Integer note) {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }

        Proposal proposal = Proposal.find.byId(idProposal);

        VoteStatusEnum voteStatus = VoteStatus.getVoteStatus();
        if (voteStatus != VoteStatusEnum.OPEN && voteStatus != VoteStatusEnum.NOT_BEGIN) {
            return unauthorized();
        }
        if (note == null || note < 1 || note > 5) {
            return badRequest();
        }

        Vote vote = Vote.findVoteByUserAndProposal(user, proposal);
        if (vote == null) {
            vote = new Vote();
            vote.setUser(user);
            vote.setProposal(proposal);
        }
        vote.setNote(note);
        vote.save();
        return ok();
    }

    public static Result proposalStat() {

        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        ObjectNode result = Json.newObject();
        result.put("nbProposals", Proposal.findNbProposals(false));
        result.put("nbProposalDraft", Proposal.findNbProposals(true));
        result.put("nbAcceptes", Proposal.findNbProposalsAcceptes());
        result.put("nbRejetes", Proposal.findNbProposalsRejetes());
        result.put("nbVotesUser", Vote.findNbVotesUser(user));
        return ok(result);
    }


    @CsvFile(separator = ";")
    public static class ProposalCsv {

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

        @CsvColumn(value = "indicationsOrganisateurs", order = 8)
        public String indicationsOrganisateurs;


        public static ProposalCsv fromProposal(Proposal proposal) {
            ProposalCsv proposalCsv = new ProposalCsv();
            if (proposal.speaker != null) {
                proposalCsv.speakerFullName = proposal.speaker.fullname;
                for (User coSpeaker : proposal.getCoSpeakers()) {
                    proposalCsv.speakerFullName += "\n" + coSpeaker.fullname;
                }
            }


            proposalCsv.title = proposal.title;
            if (proposal.statusProposal != null) {
                proposalCsv.status = proposal.statusProposal.name();
            }

            if (VoteStatus.getVoteStatus() == VoteStatusEnum.CLOSED) {
                proposalCsv.moyenne = Vote.calculMoyenne(proposal);
            }

            if (proposal.dureePreferee != null) {
                proposalCsv.formatPrefere = proposal.dureePreferee.getLibelle();
            }


            proposalCsv.description = proposal.description;
            proposalCsv.indicationsOrganisateurs = proposal.indicationsOrganisateurs;
            return proposalCsv;
        }
    }

    public static Result getAllProposalsInCsv() {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        List<ProposalCsv> proposals = new ArrayList<ProposalCsv>();
        for (Proposal proposal : Proposal.find.all()) {
            proposals.add(ProposalCsv.fromProposal(proposal));
        }

        CsvEngine engine = new CsvEngine(ProposalCsv.class);

        StringWriter writer = new StringWriter();

        engine.writeFile(writer, proposals, ProposalCsv.class);

        response().setContentType("application/octet-stream");
        response().setHeader("Content-Description", "File Transfer");
        response().setHeader("Content-Disposition", "attachment;filename=proposals.csv");
        response().setHeader("Content-Transfer-Encoding", "binary");
        response().setHeader("Expires", "0");
        response().setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response().setHeader("Pragma", "public");
        return ok(writer.toString(), "ISO-8859-1");
    }
}