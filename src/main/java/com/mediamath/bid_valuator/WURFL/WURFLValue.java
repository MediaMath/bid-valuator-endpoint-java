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

import com.google.common.base.Strings;

public class WURFLValue {
    private String left;
    private String right;
    private boolean targeted = false;

    public WURFLValue() {
    }

    /**
     * Takes a raw Helper text value, and a flag to indicate whether or not this value was targeted by the strategy
     * that resulted in the source bid opportunity. Helper values are strings, and are made up of at least one element,
     * and optionally a second, delimited by a ':'.
     * The first element is stored here as "left" {@link WURFLValue#getLeft()}, and the second as "right" {@link WURFLValue#getRight()}.
     * The presence of the second/right hand element can be tested by using {@link WURFLValue#isTwoPart()}.
     * Each WURFLValue class should have an associated {@link Helper.Dimension}, which is returned by {@link WURFLValue#getDimension()}.
     * @param wurflText The String contents of the target Helper value (e.g. "br_Chrome:ve_72.0.3626")
     * @param targeted Whether or not this value was targeted by the strategy
     */
    public WURFLValue(String wurflText, boolean targeted) {
        if (Strings.isNullOrEmpty(wurflText)) {
            throw new IllegalArgumentException("null or empty Helper value");
        }
        String[] parts = wurflText.split(":");
        setLeft(parts[0]);
        if (parts.length > 1) {
            setRight(parts[1]);
        }
        this.targeted = targeted;
    }

    public String getLeft() {
        return this.left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    /**
     *
     * @return Whether or not the second, or right hand side component is present in this Helper value
     */
    public boolean isTwoPart() {
        return !Strings.isNullOrEmpty(this.right);
    }

    /**
     * Retrieves the right hand side of a parsed Helper value, if present, according to {@link WURFLValue#isTwoPart()}
     * @return The right hand/second side of the Helper value
     */
    public String getRight() {
        return this.right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    /**
     *
     * @return Was this value targeted by the strategy?
     */
    public boolean isTargeted() {
        return this.targeted;
    }

    /**
     * Gets the associated enum value for this Helper Type
     * @return The {@link Helper.Dimension} associated with this Helper value type.  Base class returns {@link Helper.Dimension#UNKNOWN}.
     */
    public Helper.Dimension getDimension() {
        return Helper.Dimension.UNKNOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WURFLValue)) return false;

        WURFLValue that = (WURFLValue) o;

        if (isTargeted() != that.isTargeted()) return false;
        if (!getLeft().equals(that.getLeft())) return false;
        return getRight() != null ? getRight().equals(that.getRight()) : that.getRight() == null;

    }

    @Override
    public int hashCode() {
        int result = getLeft().hashCode();
        result = 31 * result + (getRight() != null ? getRight().hashCode() : 0);
        result = 31 * result + (isTargeted() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WURFLValue{" +
                "left='" + left + '\'' +
                ", right='" + right + '\'' +
                ", targeted=" + targeted +
                '}';
    }
}
