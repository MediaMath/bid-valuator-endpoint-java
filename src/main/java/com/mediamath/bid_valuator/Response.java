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

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * The respone returned by a Bid Valuator endpoint indicating the desired CPM bid and selected campaign, strategy, creative, and
 * PMP Deal Id (if present)
 */
public final class Response {
    @SerializedName("CampaignID")
    private String campaignID;
    @SerializedName("StrategyID")
    private String strategyID;
    @SerializedName("CPM")
    private double cpm;
    @SerializedName("CreativeID")
    private String creativeID;
    @SerializedName("PmpDealID")
    private String pmpDealID = null;

    public Response() {

    }

    public Response(String campaignID, String strategyID, double cpm, String creativeID, String pmpDeal) {
        setCampaignID(campaignID);
        setStrategyID(strategyID);
        setCpm(cpm);
        setCreativeID(creativeID);
        setPmpDealID(pmpDeal);
    }

    //Copy constructor
    public Response(Response other) {
        setCampaignID(other.getCampaignID());
        setStrategyID(other.getStrategyID());
        setCpm(other.getCpm());
        setCreativeID(other.getCreativeID());
        setPmpDealID(other.getPmpDealID());
    }

    @Override
    public String toString() {
        return "Response{" +
                "campaignID='" + campaignID + '\'' +
                " strategyID='" + strategyID + '\'' +
                ", cpm=" + cpm +
                ", creativeID='" + creativeID + '\'' +
                ", pmpDealID='" + pmpDealID + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return Double.compare(response.getCpm(), getCpm()) == 0 &&
                getCampaignID().equals(response.getCampaignID()) &&
                getStrategyID().equals(response.getStrategyID()) &&
                getCreativeID().equals(response.getCreativeID()) &&
                Objects.equals(getPmpDealID(), response.getPmpDealID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCampaignID(), getStrategyID(), getCpm(), getCreativeID(), getPmpDealID());
    }

    public String getCampaignID() {
        return campaignID;
    }

    public void setCampaignID(String campaignID) {
        this.campaignID = campaignID;
    }

    public double getCpm() {
        return cpm;
    }

    public void setCpm(double cpm) {
        this.cpm = cpm;
    }

    public String getCreativeID() {
        return creativeID;
    }

    public void setCreativeID(String creativeID) {
        this.creativeID = creativeID;
    }

    public String getPmpDealID() { return pmpDealID; }

    public void setPmpDealID(String pmpDealID) {
        this.pmpDealID = pmpDealID;
    }

    public String getStrategyID() {
        return strategyID;
    }

    public void setStrategyID(String strategyID) {
        this.strategyID = strategyID;
    }
}
