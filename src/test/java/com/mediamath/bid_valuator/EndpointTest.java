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

import com.google.openrtb.OpenRtb;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class EndpointTest {
    final String url = "http://localhost:4567";

    static class ServerRunner implements Runnable {
        @Override
        public void run() {
            Endpoint.main(new String[0]);
        }
    }

    static class ValidTestArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
            return Stream.of(
                    arguments(Helper.getJsonBidRequest().getBytes(), "application/json"),
                    arguments(Helper.bidRequestProtoTextWithDeal.getBytes(), "text/protobuf"),
                    arguments(Helper.binaryProtoFromText(Helper.bidRequestProtoTextNoDeal), "application/protobuf")
            );
        }
    }
    static Thread serverThread = new Thread(new ServerRunner());

    @BeforeAll
    static void startServer() {
        serverThread.run();
    }

    @AfterAll
    static void stopServer() {
        serverThread.stop();
    }



    @ParameterizedTest
    @ArgumentsSource(ValidTestArgumentsProvider.class)
    void testValidRequestRequest(byte[] request, String contentType) throws IOException {
        HttpResponse response = Helper.sendPost(request, contentType);
        assertThat(response.getStatusLine().getStatusCode())
                .isEqualTo(HttpStatus.SC_OK);
        HeaderElement[] elements = response.getEntity().getContentType().getElements();
        assertThat(response.getEntity().getContentType().getElements())
                .extracting(HeaderElement::getName)
                .contains("application/json");
        assertThat(Helper.getResponse(response))
                .isNotNull();
    }

    @Test
    void testHealthz() throws IOException {
        HttpGet request = new HttpGet(Helper.endpointURL + "/healthz");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);
        assertThat(response.getStatusLine().getStatusCode())
                .isEqualTo(HttpStatus.SC_OK);

    }

    @Test
    void testUnknownContentType() throws IOException {
        HttpResponse response = Helper.sendPost(Helper.getJsonBidRequest().getBytes(), "bad/content/type");
        assertThat(response.getStatusLine().getStatusCode())
                .isEqualTo(HttpStatus.SC_NOT_IMPLEMENTED);
    }

    @ParameterizedTest
    @ValueSource(strings={"application/json", "text/protobuf", "application/protobuf"})
    void testInvalidBidRequest(String contentType) throws IOException {
        HttpResponse response = Helper.sendPost("This is not a real bid request".getBytes(), contentType);
        assertThat(response.getStatusLine().getStatusCode())
                .isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {Helper.bidRequestProtoTextWithDeal, Helper.bidRequestProtoTextNoDeal})
    public void testValuate(String protobufText) throws Exception {
        OpenRtb.BidRequest.Builder bidRequestBuilder = OpenRtb.BidRequest.newBuilder();
        Helper.textFormatParser.merge(new StringReader(protobufText), bidRequestBuilder);
        OpenRtb.BidRequest bidRequest = bidRequestBuilder.build();
        Response response = Endpoint.valuate(bidRequest);

        // The strategy ID in the response should actually be present in the available ones
        List<OpenRtb.MM_Ext.SelectedEntity> selectedEntities = bidRequest.getExt().getMmExt().getSelectedEntitiesList();
        assertThat(selectedEntities).
                extracting(ent -> ent.getCompanionData().getStrategyID()).
                contains(response.getStrategyID());

        OpenRtb.MM_Ext.SelectedEntity selectedEntity = selectedEntities.stream()
                .filter(e -> e.getCompanionData().getStrategyID().equals(response.getStrategyID()))
                .collect(Collectors.toList()).get(0);

        assertThat(selectedEntity.getCampaignID()).isEqualTo(response.getCampaignID());

        // ... and the creative ID should be part of that strategy...
        assertThat(selectedEntity.getCompanionData().getCreativesList()).
                extracting(OpenRtb.MM_Ext.CompanionData.Creative::getID)
                .contains(response.getCreativeID());

        // ...and the CPM should be in the right range
        assertThat(response.getCpm()).isBetween(Endpoint.minCPM, Endpoint.maxCPM);
        // ...and the PMPDealID should be part of the selected strategy if there are any, or blank if not
        if(selectedEntity.getCompanionData().getPMPDealsCount() > 0) {
            assertThat(selectedEntity.getCompanionData().getPMPDealsList())
                    .extracting(OpenRtb.MM_Ext.CompanionData.PMPDeal::getID)
                    .contains(response.getPmpDealID());
        } else {
            assertThat(response.getPmpDealID()).isNullOrEmpty();
        }
    }
}