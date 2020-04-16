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
import com.google.openrtb.OpenRtb;
import winnotice.Winnotice;
import com.google.protobuf.TextFormat;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import spark.utils.IOUtils;

import java.io.*;
import java.util.Collections;
import java.util.Map;

public class Helper {
    final static ClassLoader classLoader = Helper.class.getClassLoader();
    final static String endpointURL = "http://localhost:4567";
    final static String bidRequestProtoTextWithDeal = "id:\"t1Aqq26oVkZP6fYVGsR5JW\" imp:<id:\"1\" video:<mimes:\"2\" linearity:LINEAR minduration:15 maxduration:30 protocol:VAST_1_0 w:1024 h:552 startdelay:0 skip:0 minbitrate:0 maxbitrate:0 playbackmethod:AUTO_PLAY_SOUND_ON pos:ABOVE_THE_FOLD api:0 > instl:1 tagid:\"com.playgendary.kickthebuddy-vast\" secure:1 pmp:<private_auction:1 deals:<id:\"Unity-MM-0034\" bidfloor:10 at:SECOND_PRICE > deals:<id:\"Unity-0024\" bidfloor:10 at:SECOND_PRICE > deals:<id:\"Unity-MM-0025\" bidfloor:8 at:SECOND_PRICE > deals:<id:\"Unity-MM-R1\" bidfloor:14 at:SECOND_PRICE > deals:<id:\"Unity-MM-0039\" bidfloor:12 at:SECOND_PRICE > > > app:<id:\"com.playgendary.kickthebuddy\" name:\"Kick the Buddy\" bundle:\"com.playgendary.kickthebuddy\" storeurl:\"https://play.google.com/store/apps/details?id=com.playgendary.kickthebuddy&hl=en\" > device:<ua:\"Mozilla/5.0 (Linux; Android 7.1.2; RCT6973W43R Build/NHG47K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/72.0.3626.105 Safari/537.36\" ip:\"73.154.58.52\" geo:<lat:40.1404 lon:-79.92 > language:\"en\" js:0 devicetype:TABLET ifa:\"d1eee38a-5059-4792-a04b-a02bbad90890\" lmt:0 > user:<buyeruid:\"d1eee38a-5059-4792-a04b-a02bbad90890\" > ext:<header:<type:0 > mm_ext:<AllowedLatency:20 SelectedEntities:<CampaignID:\"612476\" Pacing:<PI:9932 PacingIntervalPercentThru:70 > CompanionData:<StrategyID:\"4229124\" StrategyGoalType:\"CPA\" StrategyGoalValue:\"10000000\" PmpFloorPriceInMicro:\"0\" CrossDeviceCklessFlags:\"0\" Creatives:<ID:\"6438114\" Height:240 Width:320 > Creatives:<ID:\"6438117\" Height:300 Width:400 > Creatives:<ID:\"6438118\" Height:240 Width:320 > Creatives:<ID:\"6445187\" Height:300 Width:400 > MarketDeals:<MarketExchangeID:\"1005\" PMPDeals:<ID:\"325783\" IsGlobal:false MinBidPrice:10 > > TargetValues:<fields:<key:\"24\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"br_in-App\" > > > > > > > > > > fields:<key:\"25\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"os_Android:ve_7.1.2\" > > > > > > > > > > fields:<key:\"26\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"fo_Tablet\" > > > > > > > > > > fields:<key:\"28\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<> > > fields:<key:\"2\" value:<struct_value:<> > > fields:<key:\"3\" value:<struct_value:<> > > fields:<key:\"6\" value:<struct_value:<> > > > > > fields:<key:\"29\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"ma_Generic:mo_Android 7.1\" > > > > > > > > > > > > > BidFeature:<CountryID:\"60231\" DmaID:\"80010\" RegionID:\"70040\" IspID:\"30014\" ConnSpeed:\"20003\" PublisherID:\"0\" SiteID:\"0\" ChannelID:\"0\" OSID:\"40009\" ExchangeID:\"96\" AppID:\"900638099\" Interstitial:\"true\" IdVintage:\"999\" FoldPosition:\"1\" BrowserID:\"40013\" DeviceID:\"42000\" BrowserLangID:\"28261\" ChannelType:\"9\" UUID:\"XXXXXXXX-1234-5678-XXXX-XXXXXXXXXXXX\" CID:\"XXXXXXXX-1234-5678-XXXX-XXXXXXXXXXXX\" UserSessionFreq:\"99\" UserTime:<WeekDay:4 Hour:11 Minute:42 > ViewPrcnt:\"-1\" HistCtr:\"-1.000\" VideoCompletion:\"-1.000\" IsPixelTarget:\"0\" BidInvBrowserType:\"50002\" > > > \n";
    final static String bidRequestProtoTextNoDeal = "id:\"dd7c7a8ee140081bbe31e90280daf938c6d9857c\" imp:<id:\"1\" video:<mimes:\"2\" mimes:\"8\" mimes:\"10\" linearity:LINEAR minduration:5 maxduration:60 protocol:VAST_1_0 w:400 h:300 startdelay:1 skip:0 minbitrate:0 maxbitrate:0 playbackmethod:AUTO_PLAY_SOUND_OFF pos:UNKNOWN api:VPAID_2 > instl:0 tagid:\"989870\" secure:1 > site:<id:\"94898225\" domain:\"http://ew11.ultipro.com\" cat:\"IAB4-5\" page:\"https://ew11.ultipro.com/Login.aspx\" publisher:<id:\"18222\" > > device:<ua:\"Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Safari/537.36\" ip:\"96.38.124.15\" geo:<lat:35.86 lon:-84.12 > language:\"en\" js:0 devicetype:PERSONAL_COMPUTER lmt:0 > user:<id:\"197fe992719dc37361b863bea277287b881b907f\" buyeruid:\"b18c5a8f-16c0-4700-83e6-d01065373d9b\" > ext:<header:<type:0 > mm_ext:<AllowedLatency:20 SelectedEntities:<CampaignID:\"612476\" Pacing:<PI:9618 PacingIntervalPercentThru:70 > CompanionData:<StrategyID:\"4226922\" StrategyGoalType:\"CPA\" StrategyGoalValue:\"10000000\" PmpFloorPriceInMicro:\"0\" CrossDeviceCklessFlags:\"0\" Creatives:<ID:\"6438114\" Height:300 Width:400 > Creatives:<ID:\"6438117\" Height:300 Width:400 > Creatives:<ID:\"6438118\" Height:300 Width:400 > Creatives:<ID:\"6445187\" Height:300 Width:400 > TargetValues:<fields:<key:\"24\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"br_Chrome:ve_72.0.3626\" > > > > > > > > > > fields:<key:\"25\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"os_Windows:ve_8.0.0\" > > > > > > > > > > fields:<key:\"26\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"fo_Desktop\" > > > > > > > > > > fields:<key:\"28\" value:<struct_value:<fields:<key:\"2\" value:<struct_value:<> > > fields:<key:\"3\" value:<struct_value:<> > > > > > fields:<key:\"29\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<> > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"ma_Desktop Make:mo_Desktop Model\" > > > > > > > > > > > > > BidFeature:<CountryID:\"60231\" DmaID:\"80059\" RegionID:\"70044\" IspID:\"30010\" ConnSpeed:\"20003\" PublisherID:\"18222\" SiteID:\"94898225\" ChannelID:\"543469163\" OSID:\"40010\" ExchangeID:\"9\" AppID:\"0\" Interstitial:\"false\" IdVintage:\"2\" FoldPosition:\"0\" BrowserID:\"80212\" DeviceID:\"43000\" BrowserLangID:\"28261\" ChannelType:\"2\" UUID:\"YYYYYYYY-1234-5678-YYYY-YYYYYYYYYYYY\" CID:\"YYYYYYYY-1234-5678-YYYY-YYYYYYYYYYYY\" UserSessionFreq:\"99\" UserTime:<WeekDay:4 Hour:11 Minute:42 > ViewPrcnt:\"-1\" HistCtr:\"-1.000\" VideoCompletion:\"-1.000\" IsPixelTarget:\"0\" BidInvBrowserType:\"50000\" > > > \n";
    final static String bidRequestProtoTextAllWURFL = "id:\"5bf46a74000e24150ab388d1a70070b6\" imp:<id:\"2\" banner:<w:300 h:250 pos:ABOVE_THE_FOLD battr:1 battr:6 > instl:0 bidfloor:12.219999999999999 secure:1 pmp:<private_auction:0 deals:<id:\"549644393846517582\" bidfloor:12.219999999999999 at:SECOND_PRICE > > > app:<id:\"289560144\" name:\"Pregnancy & Baby | What to Expect\" domain:\"http://itunes.apple.com/app\" cat:\"401\" bundle:\"289560144\" publisher:<id:\"1976852001\" > > device:<ua:\"Mozilla/5.0 (iPhone; CPU iPhone OS 12_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16B92\" ip:\"000.000.000.000\" geo:<lat:42 lon:-42 > language:\"en\" js:0 devicetype:HIGHEND_PHONE ifa:\"XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXX\" lmt:0 > user:<id:\"XXXXXXXXXXXX\" buyeruid:\"XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXX\" > wseat:\"31113142998\" wseat:\"5649964118\" wseat:\"40181815839\" wseat:\"12762930794\" wseat:\"47011581968\" wseat:\"3092649364\" wseat:\"10853098151\" wseat:\"10783038761\" wseat:\"3610285140\" wseat:\"17702821048\" wseat:\"51510738668\" wseat:\"3534776366\" wseat:\"16024135205\" wseat:\"3006901581\" wseat:\"3901388838\" wseat:\"45214736296\" wseat:\"5877353383\" wseat:\"29807482158\" wseat:\"21268939158\" wseat:\"14786330367\" wseat:\"15912346968\" wseat:\"42314532379\" wseat:\"22840263062\" wseat:\"43298977666\" wseat:\"11508320488\" wseat:\"5760106791\" wseat:\"3971013429\" wseat:\"11718993907\" wseat:\"15437452256\" wseat:\"39885382245\" wseat:\"14456990233\" wseat:\"48982931248\" wseat:\"3271895764\" wseat:\"7183633132\" wseat:\"18874013582\" wseat:\"40971531739\" wseat:\"43144088308\" wseat:\"9405180985\" wseat:\"9440267792\" wseat:\"3976282130\" wseat:\"4192916754\" wseat:\"25938814977\" wseat:\"4348394926\" wseat:\"6271505710\" wseat:\"45163592587\" wseat:\"36101363996\" wseat:\"16046012288\" wseat:\"32186819938\" wseat:\"35289601663\" wseat:\"8070550464\" wseat:\"3899147268\" wseat:\"47723858814\" wseat:\"3333340592\" wseat:\"16848029211\" wseat:\"34748071809\" wseat:\"5062028607\" wseat:\"3614890145\" wseat:\"4446196523\" wseat:\"4467495465\" wseat:\"52663404361\" wseat:\"10500160390\" wseat:\"6516780578\" wseat:\"29297559190\" wseat:\"3043901806\" wseat:\"39604319991\" wseat:\"6986995588\" wseat:\"5735719922\" wseat:\"21544322721\" wseat:\"20819127566\" wseat:\"50856395431\" wseat:\"44450591893\" wseat:\"33152559773\" wseat:\"29701948683\" wseat:\"7221473773\" wseat:\"4784651162\" wseat:\"11977655337\" wseat:\"4675095434\" wseat:\"12089481885\" wseat:\"20035004448\" wseat:\"41495992218\" wseat:\"44714571244\" wseat:\"3649946961\" wseat:\"3564258421\" wseat:\"5455479825\" wseat:\"30061326753\" wseat:\"5963559108\" wseat:\"1762669852\" ext:<header:<type:0 > mm_ext:<AllowedLatency:20 SelectedEntities:<CampaignID:\"516365\" Pacing:<PI:203 PacingIntervalPercentThru:50 > CompanionData:<StrategyID:\"3490750\" StrategyGoalType:\"CPC\" StrategyGoalValue:\"300000\" PmpFloorPriceInMicro:\"0\" CrossDeviceCklessFlags:\"0\" Creatives:<ID:\"6185828\" Height:250 Width:300 > TargetValues:<fields:<key:\"24\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<values:<string_value:\"br_firefox\" > > > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"br_firefox:ve_46.1.2\" > > > > > > > > > > fields:<key:\"25\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<values:<string_value:\"os_iOS\" > > > > fields:<key:\"untargeted\" value:<list_value:<values:<string_value:\"os_iOS:ve_12.1.0\" > > > > > > > > > > fields:<key:\"26\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<values:<string_value:\"fo_Smartphone\" > > > > fields:<key:\"untargeted\" value:<list_value:<> > > > > > > > > fields:<key:\"27\" value:<struct_value:<fields:<key:\"1027\" value:<struct_value:<fields:<key:\"matched\" value:<list_value:<> > > > > > > > > fields:<key:\"28\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<> > > fields:<key:\"2\" value:<struct_value:<> > > fields:<key:\"3\" value:<struct_value:<> > > fields:<key:\"6\" value:<struct_value:<> > > > > > fields:<key:\"29\" value:<struct_value:<fields:<key:\"1\" value:<struct_value:<fields:<key:\"targeted\" value:<list_value:<values:<string_value:\"ma_Apple:mo_iPhone\" > > > > fields:<key:\"untargeted\" value:<list_value:<> > > > > > > > > > > > BidFeature:<CountryID:\"60231\" DmaID:\"80040\" RegionID:\"70034\" IspID:\"30001\" ConnSpeed:\"20003\" PublisherID:\"1976852001\" SiteID:\"1285284662\" ChannelID:\"401\" OSID:\"40009\" ExchangeID:\"4\" AppID:\"289560144\" Interstitial:\"0\" IdVintage:\"999\" FoldPosition:\"1\" BrowserID:\"40012\" DeviceID:\"41000\" BrowserLangID:\"28261\" ChannelType:\"8\" UUID:\"XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXX\" CID:\"XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXX\" UserSessionFreq:\"99\" UserTime:<WeekDay:2 Hour:15 Minute:11 > ViewPrcnt:\"60\" HistCtr:\"-1.000\" VideoCompletion:\"-1.000\" IsPixelTarget:\"0\" BidInvBrowserType:\"50002\" > > > \n";
    final static String winnotifyProtoText = "ExchangeAuctionID:\"1\" AuctionID:2 BidPriceCpm:3.1 ClearPriceCpm:4.1 TotalSpend:5.1 MmUuid:\"6\" AdvertiserID:7 CampaignID:8 StrategyID:9 CreativeID:10 BidTimestamp:\"11\"\n";
    static TextFormat.Parser textFormatParser = TextFormat.getParser();

    /**
     *
     * @param protoText A string containing the text encoded protobuf BidRequest record
     * @return A BidRequest Builder initialized from the text format parse of {@code protoText}.
     * @throws IOException
     */
    protected static OpenRtb.BidRequest.Builder bidRequestBuilderFromText(String protoText) throws IOException {
        OpenRtb.BidRequest.Builder bidRequestBuilder = OpenRtb.BidRequest.newBuilder();
        textFormatParser.merge(new StringReader(protoText), bidRequestBuilder);
        return bidRequestBuilder;
    }

    static OpenRtb.BidRequest allWURFLBidRequest() throws IOException {
        OpenRtb.BidRequest.Builder builder = bidRequestBuilderFromText(Helper.bidRequestProtoTextAllWURFL);
        return builder.build();
    }

    static HttpResponse sendPost(byte[] contents, String contentType, Map<String,String> headers) throws IOException {
        HttpPost request = new HttpPost(endpointURL + "/valuate");
        for (Map.Entry<String,String> entry:headers.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
        ByteArrayEntity body = new ByteArrayEntity(contents);
        request.addHeader("Content-Type", contentType);
        request.setEntity(body);
        return HttpClientBuilder.create().build().execute(request);
    }

    static HttpResponse sendPost(byte[] contents, String contentType) throws IOException {
        return sendPost(contents, contentType, Collections.emptyMap());
    }

    static HttpResponse sendPostWinNotice(byte[] contents, String contentType, Map<String,String> headers) throws IOException {
        HttpPost request = new HttpPost(endpointURL + "/winnotice");
        for (Map.Entry<String,String> entry:headers.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
        ByteArrayEntity body = new ByteArrayEntity(contents);
        request.addHeader("Content-Type", contentType);
        request.setEntity(body);
        return HttpClientBuilder.create().build().execute(request);
    }

    static HttpResponse sendPostWinNotice(byte[] contents, String contentType) throws IOException {
        return sendPostWinNotice(contents, contentType, Collections.emptyMap());
    }

    static Response getResponse(HttpResponse response) throws IOException {
        Gson gson = new Gson();
        return  gson.fromJson(new InputStreamReader(response.getEntity().getContent()), Response.class);
    }

    static String getJsonBidRequest() throws IOException {
        File jsonFile = new File(classLoader.getResource("bid.json").getFile());
        FileInputStream fis = new FileInputStream(jsonFile);
        StringWriter sw = new StringWriter();
        IOUtils.copy(fis, sw);
        return sw.toString();
    }

    static String getJsonWinNotice() throws IOException {
        File jsonFile = new File(classLoader.getResource("winnotice.json").getFile());
        FileInputStream fis = new FileInputStream(jsonFile);
        StringWriter sw = new StringWriter();
        IOUtils.copy(fis, sw);
        return sw.toString();
    }

    static byte[] binaryProtoFromText(String protoText) throws IOException {
        OpenRtb.BidRequest.Builder bidRequestBuilder = OpenRtb.BidRequest.newBuilder();
        textFormatParser.merge(new StringReader(protoText), bidRequestBuilder);
        OpenRtb.BidRequest bidRequest = bidRequestBuilder.build();
        return bidRequest.toByteArray();
    }

    static byte[] binaryWinNotifyProtoFromText(String protoText) throws IOException {
        Winnotice.WinNotification.Builder winnotifyBuilder = Winnotice.WinNotification.newBuilder();
        textFormatParser.merge(new StringReader(protoText), winnotifyBuilder);
        Winnotice.WinNotification winnotice = winnotifyBuilder.build();
        return winnotice.toByteArray();
    }
}
