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

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.codehaus.jackson.JsonNode;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static play.libs.Json.parse;
import static play.libs.Jsonp.jsonp;

public class ProgrammeController extends Controller {

    private static JsonNode getProgrammeJson() throws IOException {

        InputStream programmeStream = ProgrammeController.class.getResourceAsStream("/breizhcamp.json");
        try {
            return parse(CharStreams.toString(new InputStreamReader(programmeStream, "UTF-8")));
        } finally {
            Closeables.closeQuietly(programmeStream);
        }
    }

    public static Result programme() throws IOException {
        return ok(getProgrammeJson());
    }

    public static Result programmeAsJsonp(String callback) throws IOException {
        return ok(jsonp(callback, getProgrammeJson()));
    }


}
