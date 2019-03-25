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

package com.mediamath.bid_valuator.WURFL;

/**
 * Helper Device ({@link Helper.Dimension#DEVICE_MFR_AND_MODEL}) information, consisting of Manufacturer version (anything that follows ma_ )
 * and Model (anything that follows mo_)
 * For example, "ma_samsung:mo_sm-g360p" consists of manufacturer "ma_samsung" and model "mo_sm-g360p"
 */
public class Device extends WURFLValue {
    public Device() {
        super();
    }
    public Device(String wurflText, boolean targeted) {
        super(wurflText, targeted);
    }

    public String getManufacturer() {
        return getLeft();
    }

    public boolean hasModel() {
        return isTwoPart();
    }

    public String getModel() {
        return getRight();
    }

    public Helper.Dimension getDimension() {
        return Helper.Dimension.DEVICE_MFR_AND_MODEL;
    }

}
