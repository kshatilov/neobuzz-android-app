package com.shatilov.neobuzz.common;

import java.util.Arrays;

public class Hand {
    /* for each finger 0 is relaxed, 1 is fully bent */
    private double[] fingerPositions = {0, 0, 0, 0, 0};
    private double[] pressure = {0, 0 , 0};
    private String gestureCmd;

    public static final String[] simpleGestures = {
            ("0. 0. 0. 0. 0."),   // palm
            ("1. 1. 1. 1. 1."),   // fist
            ("0. 1. 1. 1. 1."),   // thumb
            ("1. 0. 1. 1. 1."),   // point
    };

    public Hand() {
    }

    public Hand(double[] pos) {
        this.fingerPositions = pos;
    }

    public String getGesture() {
        gestureCmd = Arrays.toString(fingerPositions);
        return gestureCmd;
    }

    public void setGesture(String gesture) {
        this.gestureCmd = gesture;
        string2float();
    }

    public double[] getFingerPositions() {
        return fingerPositions;
    }

    public void bendFinger(int index, double position) {
        fingerPositions[index] = position;
    }

    private void string2float() {
        String[] values = gestureCmd.split(" ");
        for (int i = 0; i < 5; i++) {
            fingerPositions[i] = Double.parseDouble(values[i]);
        }
    }

    public double[] getPressure() {
        return pressure;
    }

    public void setPressure(int index, double value) {
        if (0 <= index && index < pressure.length) {
            pressure[index] = value;
        }
    }
}

