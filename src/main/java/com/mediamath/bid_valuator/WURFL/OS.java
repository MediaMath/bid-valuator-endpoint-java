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
 * Helper Operating System ({@link Helper.Dimension#OS}) information, consisting of the OS name (anything that follows os_ ),
 * and OS version (anything that follows ve_)
 * For example, "os_windows:ve_10.0.0" consists of name "os_windows" and version "ve_10.0.0"
 */
public class OS extends WURFLValue {
    public OS() {
        super();
    }
    public OS(String wurflText, boolean targeted) {
        super(wurflText, targeted);
    }

    public String getName() {
        return getLeft();
    }

    public boolean hasVersion() {
        return isTwoPart();
    }

    public String getVersion() {
        return getRight();
    }

    public Helper.Dimension getDimension() {
        return Helper.Dimension.OS;
    }
}
