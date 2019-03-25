package com.mediamath.bid_valuator.WURFL;

/**
 * Helper Device Form Factor ({@link Helper.Dimension#DEVICE_FORM_FACTOR}) information (anything that follows "fo_").
 * For example, "fo_smartphone"
 */
public class DeviceFormFactor extends WURFLValue {
    public DeviceFormFactor() {
        super();
    }

    public DeviceFormFactor(String wurflText, boolean targeted) {
        super(wurflText, targeted);
    }

    public String getFormFactor() {
        return getLeft();
    }

    public Helper.Dimension getDimension() {
        return Helper.Dimension.DEVICE_FORM_FACTOR;
    }
}
