package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import models.utils.Mail;
import org.pegdown.PegDownProcessor;
import play.i18n.Messages;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.util.*;

import static play.libs.Json.toJson;

@SecureSocial.SecuredAction(ajaxCall = true)
public class Admin extends BaseController {

    public static Result editProfil(Long id) {
        User userRequest = getLoggedUser();
        if (!userRequest.admin) {
            return forbidden();
        }
        User userToEdit = User.findById(id);

        JsonNode node = request().body().asJson();

        if (node != null && node.get("description") != null && !node.get("description").equals("null")) {
            String description = node.get("description").asText();
            userToEdit.description = description;
            userToEdit.save();
            return ok();
        }  else {
            Map<String, List<String>> errors = new HashMap<String, List<String>>();
            errors.put("descriptionE", Collections.singletonList(Messages.get("error.required")));
            return badRequest(toJson(errors));
        }


    }

    public static Result deleteCompte(Long id) {
        User userRequest = getLoggedUser();
        if (!userRequest.admin) {
            return forbidden();
        }

        User userToDelete = User.findById(id);

        if (userToDelete.admin) {
            return badRequest();
        }
        // has proposals ?
        List<Proposal> proposals = Proposal.findBySpeaker(userToDelete);
        for (Proposal proposal : proposals) {
            proposal.status = Proposal.Status.REJECTED;
            proposal.setSpeaker(null);
            proposal.save();
        }

        //has comments ?
        List<Comment> comments = Comment.findByAuthor(userToDelete);
        for (Comment comment : comments) {

            comment.author = null;
            comment.save();
        }


        userToDelete.delete();

        return ok();
    }

    public static Result submitUsers() {
        User userRequest = getLoggedUser();
        if (!userRequest.admin) {
            return forbidden();
        }
        JsonNode node = request().body().asJson();
        Iterator<Map.Entry<String, JsonNode>> iteratorMails = node.fields();
        while (iteratorMails.hasNext()) {
            Map.Entry<String, JsonNode> entry = iteratorMails.next();
            String mail = entry.getKey();
            boolean admin = entry.getValue().asBoolean();
            User user = User.findByEmail(mail);
            if (admin) {
                if (user != null && !user.admin) {
                    user.admin = true;
                    user.save();
                }
            } else {
                if (user != null && user.admin) {
                    user.admin = false;
                    user.save();
                }
            }
        }
        return ok();
    }

    public static class ResultVote {

        public ResultVote(VoteStatusEnum status) {
            this.status = status;
        }

        public VoteStatusEnum status;
    }

    public static Result getVoteStatus() {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        return ok(toJson(new ResultVote(VoteStatus.getVoteStatus())));
    }

    public static Result changeVoteStatus(String newStatus) {
        User user = getLoggedUser();
        if (!user.admin) {
            return forbidden();
        }
        VoteStatus.changeVoteStatus(VoteStatusEnum.valueOf(newStatus));
        return ok();
    }

    public static Result mailing(String status) {
        return mailing(Proposal.Status.fromValue(status));
    }

    public static Result mailing(Proposal.Status status) {
        JsonNode body = request().body().asJson();
        String subjet = body.get("subject").asText();
        String mailMarkdown = body.get("mail").asText();

        PegDownProcessor pegDownProcessor = new PegDownProcessor();
        String mailHtml = pegDownProcessor.markdownToHtml(mailMarkdown);

        Set<String> mailsOfSpeakers = new HashSet<String>();

        for (Proposal proposal : Proposal.findByStatus(status,getEvent())) {
            if (proposal.getSpeaker() != null && proposal.getSpeaker().email != null) {
                mailsOfSpeakers.add(proposal.getSpeaker().email);
            }
            for (User coSpeakers : proposal.getCoSpeakers()) {
                if (coSpeakers.email != null) {
                    mailsOfSpeakers.add(coSpeakers.email);
                }
            }
        }

        if (!mailsOfSpeakers.isEmpty()) {
            Mail.sendMail(new Mail.Envelop(subjet, mailHtml, mailsOfSpeakers));
        }

        return ok();
    }
}