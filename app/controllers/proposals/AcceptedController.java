/*
 * Copyright 2013- Yan Bonnel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers.proposals;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import fr.ybonnel.csvengine.CsvEngine;
import fr.ybonnel.csvengine.annotation.CsvColumn;
import fr.ybonnel.csvengine.annotation.CsvFile;
import models.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.StringWriter;
import java.util.*;

import static play.libs.Jsonp.jsonp;

public class AcceptedController extends Controller {

    public static Result acceptedProposalByIdJsonp(Long id, String callback) {
        Proposal proposal = Proposal.findByIdWithFetch(id);

        if (proposal == null) {
            return notFound();
        }

        ObjectNode proposalJson = proposalToJson(proposal);
        return ok(jsonp(callback, proposalJson));
    }

    public static Result acceptedProposalById(Long id) {
        Proposal proposal = Proposal.findByIdWithFetch(id);

        if (proposal == null) {
            return notFound();
        }

        ObjectNode proposalJson = proposalToJson(proposal);
        return ok(proposalJson);
    }

    private static ObjectNode proposalToJson(Proposal proposal) {
        ObjectNode proposalJson = Json.newObject();
        proposalJson.put("id", proposal.getId());
        proposalJson.put("title", proposal.getTitle());
        proposalJson.put("description", proposal.getDescription());
        proposalJson.put("indicationsOrganisateurs", proposal.getIndicationsOrganisateurs());
        ArrayNode tags = new ArrayNode(JsonNodeFactory.instance);
        for (Tag tag : proposal.getTags()) {
            tags.add(tag.nom);
        }
        proposalJson.put("tags", tags);

        ArrayNode speakers = new ArrayNode(JsonNodeFactory.instance);
        if (proposal.getSpeaker() != null) {
            speakers.add(getSpeakerInJson(proposal.getSpeaker()));
        }
        for (User otherSpeaker : proposal.getCoSpeakers()) {
            speakers.add(getSpeakerInJson(otherSpeaker));
        }
        proposalJson.put("speakers", speakers);
        return proposalJson;
    }

    public static Result acceptedSpeakers() {
        return ok(getAcceptedProposalsToJson());
    }

    @CsvFile(separator = ";")
    private static class AdressMacForSpeakers {

        public AdressMacForSpeakers(String speaker, String mac, String mail, String proposals) {
            this.speaker = speaker;
            this.mac = mac;
            this.mail = mail;
            this.proposals = proposals;
        }

        public AdressMacForSpeakers() {
        }

        @CsvColumn("speaker_fullname")
        public String speaker;

        @CsvColumn("speaker_mail")
        public String mail;

        @CsvColumn("proposals")
        public String proposals;

        @CsvColumn("@MAC")
        public String mac;
    }

    public static Result adressMacOfAcceptedSpeakers() {

        List<Proposal> proposalsAccepted = Proposal.findByStatusForMinimalData(Proposal.Status.ACCEPTED);

        Map<User, List<Proposal>> speakers = new HashMap<User, List<Proposal>>();

        for (Proposal proposal : proposalsAccepted) {
            if (proposal.getSpeaker() != null) {
                if (!speakers.containsKey(proposal.getSpeaker())) {
                    speakers.put(proposal.getSpeaker(), new ArrayList<Proposal>());
                }
                speakers.get(proposal.getSpeaker()).add(proposal);
            }
            for (User speaker : proposal.getCoSpeakers()) {
                if (!speakers.containsKey(speaker)) {
                    speakers.put(speaker, new ArrayList<Proposal>());
                }
                speakers.get(speaker).add(proposal);
            }
        }

        List<User> speakersSorted = new ArrayList<User>(speakers.keySet());
        Collections.sort(speakersSorted, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.id.compareTo(o2.id);
            }
        });

        List<AdressMacForSpeakers> macAddressOfSpeakers = new ArrayList<AdressMacForSpeakers>();

        for (User speaker : speakersSorted) {
            macAddressOfSpeakers.add(new AdressMacForSpeakers(speaker.getFullname(), speaker.adresseMac, speaker.email, Joiner.on('\n').join(
                    Iterables.transform(speakers.get(speaker), new Function<Proposal, String>() {
                        @Override
                        public String apply(Proposal proposal) {
                            return proposal.getTitle();
                        }
                    }))));
        }


        CsvEngine engine = new CsvEngine(AdressMacForSpeakers.class);

        StringWriter writer = new StringWriter();

        engine.writeFile(writer, macAddressOfSpeakers, AdressMacForSpeakers.class);

        response().setContentType("application/octet-stream");
        response().setHeader("Content-Description", "File Transfer");
        response().setHeader("Content-Disposition", "attachment;filename=macaddress.csv");
        response().setHeader("Content-Transfer-Encoding", "binary");
        response().setHeader("Expires", "0");
        response().setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response().setHeader("Pragma", "public");
        return ok(writer.toString(), "ISO-8859-1");
    }

    public static Result acceptedSpeakersToJson(String callback) {

        ArrayNode result = getAcceptedProposalsToJson();
        return ok(jsonp(callback, result));
    }

    private static ArrayNode getAcceptedProposalsToJson() {
        List<Proposal> proposalsAccepted = Proposal.findByStatusForMinimalData(Proposal.Status.ACCEPTED);

        Map<User, List<Proposal>> speakers = new HashMap<User, List<Proposal>>();

        for (Proposal proposal : proposalsAccepted) {
            if (proposal.getSpeaker() != null) {
                if (!speakers.containsKey(proposal.getSpeaker())) {
                    speakers.put(proposal.getSpeaker(), new ArrayList<Proposal>());
                }
                speakers.get(proposal.getSpeaker()).add(proposal);
            }
            for (User speaker : proposal.getCoSpeakers()) {
                if (!speakers.containsKey(speaker)) {
                    speakers.put(speaker, new ArrayList<Proposal>());
                }
                speakers.get(speaker).add(proposal);
            }
        }

        List<User> speakersSorted = new ArrayList<User>(speakers.keySet());
        Collections.sort(speakersSorted, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.id.compareTo(o2.id);
            }
        });

        // Data used in html :
        // speaker.id
        // speaker.fullname
        // speaker.avatar
        // speaker.description
        // speaker.links.url
        // speaker.links.label
        // speaker.proposals.id
        // speaker.proposals.title
        // speaker.proposals.description
        // speaker.proposals.otherspeakers

        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
        for (User speaker : speakersSorted) {
            ObjectNode speakerJson = getSpeakerInJson(speaker);
            ArrayNode proposalsJson = new ArrayNode(JsonNodeFactory.instance);
            for (Proposal proposal : speakers.get(speaker)) {
                ObjectNode proposalJson = Json.newObject();
                proposalJson.put("id", proposal.getId());
                proposalJson.put("title", proposal.getTitle());
                proposalJson.put("description", proposal.getDescription());
                proposalJson.put("indicationsOrganisateurs", proposal.getIndicationsOrganisateurs());
                ArrayNode otherSpeakers = new ArrayNode(JsonNodeFactory.instance);
                if (proposal.getSpeaker() != null && !speaker.equals(proposal.getSpeaker())) {
                    otherSpeakers.add(getSpeakerInJson(proposal.getSpeaker()));
                }
                for (User otherSpeaker : proposal.getCoSpeakers()) {
                    if (!otherSpeaker.equals(speaker)) {
                        otherSpeakers.add(getSpeakerInJson(otherSpeaker));
                    }
                }
                proposalJson.put("otherspeakers", otherSpeakers);
                proposalsJson.add(proposalJson);
            }
            speakerJson.put("proposals", proposalsJson);
            result.add(speakerJson);
        }
        return result;
    }

    public static Result acceptedProposalsToJson(String callback) {

        // Data used in html :
        // proposal.id
        // proposal.title
        // proposal.description
        // proposal.speaker.fullname
        // proposal.speaker.avatar
        // proposal.speaker.description
        // proposal.speaker.links.url
        // proposal.speaker.links.label
        // proposal.coSpeakers

        List<Proposal> proposalsAccepted = Proposal.findByStatusForMinimalData(Proposal.Status.ACCEPTED);
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
        for (Proposal proposal : proposalsAccepted) {
            ObjectNode proposalJson = Json.newObject();
            proposalJson.put("id", proposal.getId());
            proposalJson.put("title", proposal.getTitle());
            proposalJson.put("description", proposal.getDescription());
            proposalJson.put("indicationsOrganisateurs", proposal.getIndicationsOrganisateurs());

            if (proposal.getSpeaker() != null) {
                proposalJson.put("speaker", getSpeakerInJson(proposal.getSpeaker()));
            }
            ArrayNode coSpeakers = new ArrayNode(JsonNodeFactory.instance);
            for (User speaker : proposal.getCoSpeakers()) {
                coSpeakers.add(getSpeakerInJson(speaker));
            }
            proposalJson.put("coSpeakers", coSpeakers);
            result.add(proposalJson);
        }
        return ok(jsonp(callback, result));
    }

    private static ObjectNode getSpeakerInJson(User speaker) {
        ObjectNode speakerJson = Json.newObject();
        speakerJson.put("id", speaker.id);
        speakerJson.put("fullname", speaker.getFullname());
        speakerJson.put("avatar", speaker.getAvatar());
        speakerJson.put("description", speaker.description);

        ArrayNode liens = new ArrayNode(JsonNodeFactory.instance);
        for (Link link : speaker.getLinks()) {
            ObjectNode lienJson = Json.newObject();
            lienJson.put("url", link.url);
            lienJson.put("label", link.label);
            liens.add(lienJson);
        }
        speakerJson.put("links", liens);
        return speakerJson;
    }
}
