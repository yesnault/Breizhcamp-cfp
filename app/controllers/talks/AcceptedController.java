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
package controllers.talks;

import models.Lien;
import models.StatusTalk;
import models.Talk;
import models.User;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static play.libs.Jsonp.jsonp;

public class AcceptedController extends Controller {

    public static Result acceptedTalksToJson(String callback) {

        // Data used in html :
        // talk.id
        // talk.title
        // talk.description
        // talk.speaker.fullname
        // talk.speaker.avatar
        // talk.speaker.description
        // talk.speaker.liens.url
        // talk.speaker.liens.label
        // talk.coSpeakers

        List<Talk> talksAccepted = Talk.findByStatusForMinimalData(StatusTalk.ACCEPTE);
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
        for (Talk talk : talksAccepted) {
            ObjectNode talkJson = Json.newObject();
            talkJson.put("id", talk.id);
            talkJson.put("title", talk.title);
            talkJson.put("description", talk.description);

            if (talk.speaker != null) {
                talkJson.put("speaker", getSpeakerInJson(talk.speaker));
            }
            ArrayNode coSpeakers = new ArrayNode(JsonNodeFactory.instance);
            for (User speaker : talk.getCoSpeakers()) {
                coSpeakers.add(getSpeakerInJson(speaker));
            }
            talkJson.put("coSpeakers", coSpeakers);
            result.add(talkJson);
        }
        return ok(jsonp(callback, result));
    }

    private static ObjectNode getSpeakerInJson(User speaker) {
        ObjectNode speakerJson = Json.newObject();
        speakerJson.put("id", speaker.id);
        speakerJson.put("fullname", speaker.fullname);
        speakerJson.put("avatar", speaker.getAvatar());
        speakerJson.put("description", speaker.description);


        ArrayNode liens = new ArrayNode(JsonNodeFactory.instance);
        for (Lien lien : speaker.getLiens()) {
            ObjectNode lienJson = Json.newObject();
            lienJson.put("url", lien.url);
            lienJson.put("label", lien.label);
            liens.add(lienJson);
        }
        speakerJson.put("liens", liens);
        return speakerJson;
    }
}
