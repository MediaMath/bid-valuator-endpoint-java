/*
 *  Copyright 2019 MediaMath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.mediamath.bid_valuator;

import com.google.gson.Gson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseJsonTransformerTest {
    private ResponseJsonTransformer transformer = new ResponseJsonTransformer();

    static Stream<Response> responseProvider() {
        return Stream.of(
                // With Deal
                new Response("12345", "5678", 0.1234, "7890", "abc123"),
                // No/Null Deal ID
                new Response("12345", "5678", 0.1234, "7890", null),
                //Empty Deal ID
                new Response("12345", "5678", 0.1234, "7890", "")
        );
    }

    @ParameterizedTest
    @MethodSource("responseProvider")
    public void testJsonTransform(Response response) {
        String responseJson = transformer.render(response);
        Gson gson = new Gson();
        Response result = gson.fromJson(responseJson, Response.class);
        assertThat(response).isEqualTo(result);
    }
}