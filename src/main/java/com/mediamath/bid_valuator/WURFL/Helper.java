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

import com.google.openrtb.OpenRtb;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A helper class to aid in decoding the Helper values used for device classification,
 * which are somewhat incomprehensible to a normal human.  I do not know _why_ Helper
 * data is structured the way it is, but this should help make it more useful.
 *
 * @see <a href="http://wurfl.sourceforge.net/">Helper</a>
 */
public class Helper {
    /**
     * The values we're after are always found under {@code "DimensionNo": {"1": ...}}
     */
    public static final String DIMENSION_SUBKEY = "1";
    public static final String TARGETED = "targeted";
    public static final String UNTARGETED = "untargeted";
    private static Value emptyList = Value.newBuilder().clearListValue().build();

    private Helper() {}

    /**
     * Extracts all the Helper data for a given type (as defined by {@link Dimension}
     * @param companionData The mm_ext CompanionData object parsed from the bid request protobuf,
     *                      each SelectedEntity contained in the MediaMath enrichment extensions to the OpenRTB
     *                      bid request schema (found under ext-&gt;mm_ext) may contain one or more of the available
     *                      Helper dimensions, which may be targeted by the associated strategy.
     *
     *                      <p>Example:</p>
     *
     *       <pre>{@code
     *       OpenRtb.MM_Ext.CompanionData companionData = bidRequest.getExt().getMmExt().getSelectedEntities(0).getCompanionData();
     *       List<Browser> browserWurfl = Helper.getWURFLData(companionData, Browser.class); // <[WURFLValue{left='br_firefox', right='null', targeted=true},WURFLValue{left='br_firefox', right='ve_46.1.2', targeted=false}]>
     *       }</pre>
     * @param targetType The type of value to extract,
     * @param <T> A WURFLValue subclass ({@link Browser}, {@link OS}, {@link Device}, {@link DeviceFormFactor})
     * @return A list of T class instances representing the Helper values for the selected dimension
     * @throws IllegalArgumentException will be thrown if the targeted Helper dimension is not present in the data
     */
    public static <T extends WURFLValue> List<T> getWURFLData(OpenRtb.MM_Ext.CompanionData companionData, Class<T> targetType)
    throws IllegalArgumentException
    {
        Dimension dim = null;
        try {
            dim = targetType.newInstance().getDimension();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate WURFLValue class while determining dimension " + targetType.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access WURFLValue class constructor while determining dimension " + targetType.getName(), e);
        }

        if (dim == Dimension.UNKNOWN) {
            throw new IllegalArgumentException("Cannot get Helper data for unknown dimension " + dim.toString());
        }

        Struct targetValues = companionData.getTargetValues();
        Struct dimStruct, root;
        try {
            dimStruct = targetValues.getFieldsOrThrow(dim.getKey()).getStructValue();
        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to find " + targetType.getName() + " dimension key " + dim.getKey() + " in TargetValues ", e);
        }
        try {
            root = dimStruct.getFieldsOrThrow(DIMENSION_SUBKEY).getStructValue();
        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to dimension subkey " + DIMENSION_SUBKEY + " in Helper dimension struct " + dimStruct, e);
        }

        Constructor<T> ctor = null;
        try {
            ctor = targetType.getConstructor(String.class, boolean.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not get constructor for target type " + targetType.getName(), e);
        }
        List<Value> targetedValues = root.getFieldsOrDefault(TARGETED, emptyList).getListValue().getValuesList();
        List<Value> untargetedValues = root.getFieldsOrDefault(UNTARGETED, emptyList).getListValue().getValuesList();
        Constructor<T> finalCtor = ctor;
        List<T> data = targetedValues.stream().map(v -> {
            try {
                return finalCtor.newInstance(v.getStringValue(), true);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Could not instantiate WURFLValue class " + targetType.getName(), e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Could not access WURFLValue class constructor" + targetType.getName(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("Could not invoke constructor for WURFLValue class " + targetType.getName(), e);
            }
        }).collect(Collectors.toList());
        Constructor<T> finalCtor1 = ctor;
        untargetedValues.forEach(v -> {
            try {
                data.add(finalCtor1.newInstance(v.getStringValue(), false));
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Could not instantiate WURFLValue class " + targetType.getName(), e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Could not access WURFLValue class constructor" + targetType.getName(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("Could not invoke constructor for WURFLValue class " + targetType.getName(), e);
            }
        });
        return data;
    }

    /**
     * Convenience method for extracting {@link Browser} Helper data
     * @param selectedEntity a selected entity within the MediaMath extension to the OpenRTB schema
     * @return A list of {@link Browser} Helper data found in the selected entity (see {@link Helper#getWURFLData(OpenRtb.MM_Ext.CompanionData, Class)}
     */
    public static List<Browser> getBrowserData(OpenRtb.MM_Ext.SelectedEntity selectedEntity) {
        return getWURFLData(selectedEntity.getCompanionData(), Browser.class);
    }

    /**
     * Convenience method for extracting {@link OS} Helper data
     * @param selectedEntity a selected entity within the MediaMath extension to the OpenRTB schema
     * @return A list of {@link OS} Helper data found in the selected entity (see {@link Helper#getWURFLData(OpenRtb.MM_Ext.CompanionData, Class)}
     */
    public static List<OS> getOSData(OpenRtb.MM_Ext.SelectedEntity selectedEntity) {
        return getWURFLData(selectedEntity.getCompanionData(), OS.class);
    }

    /**
     * Convenience method for extracting {@link Device} Helper data
     * @param selectedEntity a selected entity within the MediaMath extension to the OpenRTB schema
     * @return A list of {@link Device} Helper data found in the selected entity (see {@link Helper#getWURFLData(OpenRtb.MM_Ext.CompanionData, Class)}
     */
    public static List<Device> getDeviceData(OpenRtb.MM_Ext.SelectedEntity selectedEntity) {
        return getWURFLData(selectedEntity.getCompanionData(), Device.class);
    }

    /**
     * Convenience method for extracting {@link Device} form factor Helper data
     * @param selectedEntity a selected entity within the MediaMath extension to the OpenRTB schema
     * @return A list of {@link DeviceFormFactor} Helper data found in the selected entity (see {@link Helper#getWURFLData(OpenRtb.MM_Ext.CompanionData, Class)}
     */
    public static List<DeviceFormFactor> getDeviceFormFactorData(OpenRtb.MM_Ext.SelectedEntity selectedEntity) {
        return getWURFLData(selectedEntity.getCompanionData(), DeviceFormFactor.class);
    }

    /**
     * Enumeration containing valid values for Helper dimensions (Device, Browser, OS, etc.)
     * {@link WURFLValue}
     */
    public enum Dimension {
        UNKNOWN(0),
        BROWSER(24),
        OS(25),
        DEVICE_FORM_FACTOR(26),
        DEVICE_MFR_AND_MODEL(29);
        private int dimensionNo;

        /**
         *  Dimension enum private constructor which adds the integer value associated with each name
         * @param dimensionNo The Helper numeric dimension key for the enum value
         */
        Dimension(int dimensionNo) {
            this.dimensionNo = dimensionNo;
        }

        /**
         * Returns the string version of the associated Helper numeric dimension key.  The keys in the source
         * JSON, and therefore resulting protobuf are strings, which is why we must convert
         * @return The String value for the dimension number associated with this Enum value
         */
        public String getKey() {
            return Integer.toString(this.dimensionNo);
        }
    }

}
