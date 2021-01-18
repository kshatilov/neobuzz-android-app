package com.shatilov.neobuzz.common.haptics;

public enum VibrationIntensity {
    NONE(0),
    LOW(0x40),
    MEDIUM(0x80),
    HIGH(0xFF);

    private int intensityValue;

    VibrationIntensity(int intensity) {
        this.intensityValue = intensity;
    }

    public int getValue() {
        return intensityValue;
    }
}
