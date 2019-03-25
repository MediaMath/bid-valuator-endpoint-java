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
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import com.mediamath.bid_valuator.WURFL.Device;
import com.mediamath.bid_valuator.WURFL.Helper;
import com.mediamath.bid_valuator.WURFL.WURFLValue;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static spark.Spark.*;

/**
 * <h1>Bid Valuator Sample Endpoint</h1>
 *<p>
 * This endpoint exposes two routes, and will listen on all available interfaces on port 4567:
 * <ul>
 *     <li><b>/valuate</b>: Bid valuator will send bid requests here, and will use the response to place a bid</li>
 *     <li><b>/healthz</b>: Simple health check, returns 200 OK, used for health monitoring</li>
 * </ul>
 *</p>
 * <p>
 * /valuate will accept bid requests in one of the following formats, depending on the Content-Type header:
 *</p>
 * <ul>
 *     <li>Protobuf binary encoding (application/protobuf) <b><i>This is the only format Bid Valuator will send right now, the others are for debugging or testing purposes</i></b></li>
 *     <li>JSON (application/json)</li>
 *     <li>Protbuf Text Encoding (text/protobuf)</li>
 * </ul>
 *<p>
 * See {@link Endpoint#valuate(OpenRtb.BidRequest)} for details on the response format and behavior
 * </p>
 */
public class Endpoint {
    private static TextFormat.Parser textFormatParser = TextFormat.getParser();
    private static JsonFormat.Parser jsonFormatParser = JsonFormat.parser();
    /**
     * The endpoint will use this as the lower bound of the randomly generated CPM
     */
    public static double minCPM = 0.01;
    /**
     * The endpoint will use this as the upper bound of the randomly generated CPM
     */
    public static double maxCPM = 0.5;
    private static Random random = new Random();
    private static Logger logger = LoggerFactory.getLogger("com.mediamath.bid_valuator.Endpoint");

    public static void main(String[] args) {
        post("/valuate", (req, res) -> {
            MDC.put("path", req.pathInfo());
            res.type("application/json");
            OpenRtb.BidRequest bidRequest = null;
            OpenRtb.BidRequest.Builder builder = OpenRtb.BidRequest.newBuilder();
            try {
                MDC.put("contentType", req.contentType());
                // Bid Valuator only sends binary protobuf (with Content-Type: application/protobuf), but
                // using the raw json or text protobuf encoding can be useful for testing and debugging
                if(req.contentType().equals("application/json")) {
                    jsonFormatParser.merge(new StringReader(req.body()), builder);
                    bidRequest = builder.build();
                } else if(req.contentType().equals("text/protobuf")){
                    textFormatParser.merge(new StringReader(req.body()), builder);
                    bidRequest = builder.build();
                } else if(req.contentType().equals("application/protobuf")){
                    bidRequest = OpenRtb.BidRequest.parseFrom(req.bodyAsBytes());
                } else {
                    String msg = "Content-Type " + req.contentType() + " is not supported";
                    logger.error(msg);
                    halt(HttpStatus.SC_NOT_IMPLEMENTED, msg);
                }
            } catch(IOException e) {
                MDC.put("requestBody", req.body());
                logger.error("Failed to unmarshal BidRequest: ", e);
                halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
            if(bidRequest == null) {
                String msg = "Could not unmarshal BidRequest from request body";
                logger.error(msg);
                halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
            }
            res.type("application/json");
            return valuate(bidRequest);
        }, new ResponseJsonTransformer());
        get("/healthz", (req, res) -> {
            MDC.put("path", req.pathInfo());
            awaitInitialization();
            return "OK";
        });
    }

    /**
     * Returns a bid valuator response with a made up CPM, and a randomly selected strategy, creative, and Deal.
     * Slightly alters behavior according to the Helper values present in the selected strategy's enrichments
     * as an example of the usage of Helper data (see {@link Helper}).
     *
     * This makes use of {@link org.slf4j.MDC} to provide contextual info in any generated log entries, including:
     * <ul>
     * <li>auctionID : The auction ID in the bid request</li>
     * <li>cpm : The randomly chosen CPM value</li>
     * <li>campaignID : The campaign ID containing the selected strategy</li>
     * <li>strategyID : The selected strategy ID</li>
     * <li>creativeID : The creative ID randomly chosen from the list of candidates</li>
     * <li>pmpDealID : The PMP Deal ID randomly selected from the list of candidates, if any are present.  This will be
     *                a blank string otherwise</li>
     * </ul>
     * @param req An OpenRTB BidRequest as deined by the openrtb.proto protobuf schema
     * @return The response object to be returned to Bid Valuator
     */
    public static Response valuate(OpenRtb.BidRequest req) {
        MDC.put("auctionID", req.getId());

        double cpm = minCPM + (maxCPM - minCPM) * random.nextDouble();
        MDC.put("cpm", Double.toString(cpm));

        OpenRtb.MM_Ext mmExt = req.getExt().getMmExt();
        OpenRtb.MM_Ext.SelectedEntity selectedStrategy = mmExt.getSelectedEntities(random.nextInt(mmExt.getSelectedEntitiesCount()));
        MDC.put("campaignID", selectedStrategy.getCampaignID());

        OpenRtb.MM_Ext.CompanionData companionData = selectedStrategy.getCompanionData();
        MDC.put("strategyID", companionData.getStrategyID());

        int totalDeals = companionData.getPMPDealsCount();

        logger.debug("Received bid request id {} {} total SelectedEntities selected strategy {} with {} PMPDeals",
                req.getId(),
                mmExt.getSelectedEntitiesCount(),
                companionData.getStrategyID(),
                totalDeals);

        String selectedCreativeID = companionData.getCreatives(random.nextInt(companionData.getCreativesCount())).getID();
        MDC.put("creativeID", selectedCreativeID);

        // If there are no deals, just pass a blank string
        String selectedDealID = (totalDeals > 0) ? companionData.getPMPDeals(random.nextInt(totalDeals)).getID() : "";
        MDC.put("pmpDealID", selectedDealID);

        try {
            // Just as an example, if any targeted Device data is available via Helper, increase bid price by 5%
            List<Device> deviceWURFL = Helper.getDeviceData(selectedStrategy);
            Optional<Device> targetedDevice = deviceWURFL.stream().filter(WURFLValue::isTargeted).findFirst();
            if(targetedDevice.isPresent()) {
                logger.info("Target WURFL device (model: {} manufacturer: {}) found, increasing bid by 5%",
                        targetedDevice.get().getModel(),
                        targetedDevice.get().getManufacturer());
                cpm = cpm * 1.05;
            }
        } catch(IllegalArgumentException e) {
            logger.debug("No Device WURFL data found for selected strategy, not altering bid");
        }

        Response response = new Response(selectedStrategy.getCampaignID(), companionData.getStrategyID(), cpm, selectedCreativeID, selectedDealID);
        logger.info("Returning bid of {}", cpm);
        MDC.clear();
        return response;
    }
}
