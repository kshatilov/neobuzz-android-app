package com.shatilov.neobuzz.common.haptics;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HapticProfile {

    public class GesturePattern {
        private double [] gesture;
        private ArrayList<int[]> pattern;

        public double[] getGesture() {
            return gesture;
        }

        public GesturePattern(double[] gesture, ArrayList<int[]> pattern) {
            this.gesture = gesture;
            this.pattern = pattern;
        }
    }
    
    private static final String TAG = "VibrationPattern";
    private List<GesturePattern> patterns = new ArrayList<>();
    private long interval = 500;

    public long getInterval() {
        return interval;
    }

    public List<GesturePattern> getPatterns() {
        return patterns;
    }

    public HapticProfile(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            // interval
            if (jsonObject.has("interval")) {
                interval = Long.parseLong(jsonObject.getString("interval"));
            }

            JSONArray config = jsonObject.getJSONArray("config");
            for(int i = 0; i < config.length(); i++)
            {
                JSONObject iConfig = config.getJSONObject(i);
                JSONArray gestureJArray = iConfig.getJSONArray("gesture");
                double[] gesture = new double[5];
                for (int j = 0; j < gestureJArray.length(); j++) {
                    gesture[j] = Double.parseDouble(gestureJArray.getString(j));
                }
                ArrayList<int[]> patterns = new ArrayList<>();
                JSONArray patternsJArray = iConfig.getJSONArray("pattern");
                for (int j = 0; j < patternsJArray.length(); j++) {
                    JSONArray patternJArray = patternsJArray.getJSONArray(j);
                    int[] pattern = new int[4];
                    Log.d(TAG, "VibrationPattern: " + patternJArray.length());
                    for (int k = 0; k < patternJArray.length(); k++) {
                        pattern[k] = Integer.parseInt(patternJArray.getString(k));
                    }
                    patterns.add(pattern);
                }
                this.patterns.add(new GesturePattern(gesture, patterns));
            }
        } catch (JSONException e) {
            Log.d(TAG, "VibrationPattern: Failed to load vibro pattern " + e.getLocalizedMessage());
        }
    }

    public List<int[]> getPattern(double[] targetGesture) {
        for (GesturePattern gp : patterns) {
            if (Arrays.equals(gp.gesture, targetGesture)) {
                return gp.pattern;
            }
        }
        return null;
    }
}
