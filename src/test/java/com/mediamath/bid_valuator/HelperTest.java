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

import com.google.common.base.Strings;
import com.google.openrtb.OpenRtb;
import com.mediamath.bid_valuator.WURFL.*;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class HelperTest {
    private static class DynamicWURFLTest<T extends WURFLValue> {
        Class<T> valueClass;
        String left, right, rightPropertyName;

        DynamicWURFLTest(Class<T> valueClass, String left, String right, String rightPropertyName) {
            this.valueClass = valueClass;
            this.left = left;
            this.right = right;
            this.rightPropertyName = rightPropertyName;
        }

        @Override
        public String toString() {
            return "DynamicWURFLTest{" +
                    "valueClass=" + valueClass.getName() +
                    ", left='" + left + '\'' +
                    ", right='" + right + '\'' +
                    ", rightPropertyName='" + rightPropertyName + '\'' +
                    '}';
        }
    }

    static Stream<DynamicWURFLTest> wurflTests() {
        return Stream.of(
            // WURFLValue subclass, wurflText, left, right,  right method name
            new DynamicWURFLTest<>(Browser.class, "br_firefox", "ve_46.1.2", "version"),
            new DynamicWURFLTest<>(Browser.class, "br_firefox", "ve_46.1.2", "version"),
            new DynamicWURFLTest<>(Browser.class, "br_firefox", "", ""),
            new DynamicWURFLTest<>(Device.class, "ma_samsung", "mo_sm-g360p", "model"),
            new DynamicWURFLTest<>(Device.class, "ma_samsung", "", "model"),
            new DynamicWURFLTest<>(DeviceFormFactor.class, "fo_smartphone", "unused", ""),
            new DynamicWURFLTest<>(DeviceFormFactor.class, "fo_smartphone", "", ""),
            new DynamicWURFLTest<>(OS.class, "os_windows", "ve_10.0.0", "version"),
            new DynamicWURFLTest<>(OS.class, "os_windows", "", "version")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testEmptyWURFLText(String wurflText) {
        Throwable thrown = catchThrowableOfType(() -> new WURFLValue(wurflText, true),
                IllegalArgumentException.class);
        assertThat(thrown).hasMessageContaining("null or empty Helper value");
    }

    @ParameterizedTest
    @MethodSource("wurflTests")
    <T extends WURFLValue> void testValueParse(DynamicWURFLTest<T> t) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> ctor = t.valueClass.getConstructor(String.class, boolean.class);
        T value = ctor.newInstance(t.left + ":" + t.right, true);
        assertThat(value)
                .extracting(T::getLeft)
                .first()
                .isEqualTo(t.left);
        if(!Strings.isNullOrEmpty(t.right)) {
            assertThat(value.getRight())
                    .isEqualTo(t.right);
            assertThat(value)
                    .extracting(T::isTwoPart)
                    .first()
                    .isEqualTo(true);
            if(!Strings.isNullOrEmpty(t.rightPropertyName)) {
                assertThat(BeanUtils.getSimpleProperty(value, t.rightPropertyName))
                        .isEqualTo(t.right);
            }
        } else {
            assertThat(value.getRight())
                    .isNullOrEmpty();
        }
    }

    @Test
    void testBrowserDataExtraction() throws IOException {
        OpenRtb.BidRequest bidRequest = com.mediamath.bid_valuator.Helper.allWURFLBidRequest();
        List<Browser> browserResults = com.mediamath.bid_valuator.WURFL.Helper.getWURFLData(bidRequest.getExt().getMmExt().getSelectedEntities(0).getCompanionData(), Browser.class);
        assertThat(browserResults)
                .isNotEmpty()
                .containsExactlyInAnyOrder(new Browser("br_firefox", true), new Browser("br_firefox:ve_46.1.2", false));
        assertThat(com.mediamath.bid_valuator.WURFL.Helper.getBrowserData(bidRequest.getExt().getMmExt().getSelectedEntities(0)))
                .hasSameElementsAs(browserResults);
    }

    @Test
    void testOSDataExtraction() throws IOException {
        OpenRtb.BidRequest bidRequest = com.mediamath.bid_valuator.Helper.allWURFLBidRequest();
        List<OS> osResults = com.mediamath.bid_valuator.WURFL.Helper.getWURFLData(bidRequest.getExt().getMmExt().getSelectedEntities(0).getCompanionData(), OS.class);
        assertThat(osResults)
                .isNotEmpty()
                .containsExactlyInAnyOrder(new OS("os_iOS", true), new OS("os_iOS:ve_12.1.0", false));
        assertThat(com.mediamath.bid_valuator.WURFL.Helper.getOSData(bidRequest.getExt().getMmExt().getSelectedEntities(0)))
                .hasSameElementsAs(osResults);
    }

    @Test
    void testDeviceDataExtraction() throws IOException {
        OpenRtb.BidRequest bidRequest = com.mediamath.bid_valuator.Helper.allWURFLBidRequest();
        List<Device> deviceResults = com.mediamath.bid_valuator.WURFL.Helper.getWURFLData(bidRequest.getExt().getMmExt().getSelectedEntities(0).getCompanionData(), Device.class);
        assertThat(deviceResults)
                .isNotEmpty()
                .containsExactlyInAnyOrder(new Device("ma_Apple:mo_iPhone", true));
        assertThat(com.mediamath.bid_valuator.WURFL.Helper.getDeviceData(bidRequest.getExt().getMmExt().getSelectedEntities(0)))
                .hasSameElementsAs(deviceResults);
    }

    @Test
    void testDeviceFormFactorDataExtraction() throws IOException {
        OpenRtb.BidRequest bidRequest = com.mediamath.bid_valuator.Helper.allWURFLBidRequest();
        List<DeviceFormFactor> dffResults = com.mediamath.bid_valuator.WURFL.Helper.getWURFLData(bidRequest.getExt().getMmExt().getSelectedEntities(0).getCompanionData(), DeviceFormFactor.class);
        assertThat(dffResults)
                .isNotEmpty()
                .containsExactlyInAnyOrder(new DeviceFormFactor("fo_Smartphone", true));
        assertThat(com.mediamath.bid_valuator.WURFL.Helper.getDeviceFormFactorData(bidRequest.getExt().getMmExt().getSelectedEntities(0)))
                .hasSameElementsAs(dffResults);
    }
  }