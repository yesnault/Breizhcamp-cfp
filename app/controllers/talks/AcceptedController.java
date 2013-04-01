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

import models.StatusTalk;
import models.Talk;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class AcceptedController extends Controller {

    public static Result index() {
        List<Talk> talksAccepted = Talk.findByStatus(StatusTalk.ACCEPTE);
        for (Talk talk : talksAccepted) {
            if (talk.speaker.fullname != null) {
                talk.speaker.fullname.toString();
            }
        }
        return ok(views.html.talks.accepted.render(talksAccepted));
    }
}
