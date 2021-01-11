package com.shatilov.neobuzz;

public class Hand {
    /* for each finger 0 is relaxed, 1 is fully bent */
    private double[] fingerPositions = {0, 0, 0, 0, 0};
    private String gestureCmd;

    public static final String[] simpleGestures = {
            ("0. 0. 0. 0. 0."),   // palm
            ("1. 1. 1. 1. 1."),   // fist
            ("0. 1. 1. 1. 1."),   // thumb
            ("1. 0. 1. 1. 1."),   // point
    };

    public Hand() {
    }

    public String getGesture() {
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

}

