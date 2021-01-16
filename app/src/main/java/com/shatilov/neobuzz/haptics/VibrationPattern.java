package com.shatilov.neobuzz.haptics;

import android.content.Context;
import android.util.Log;

import com.shatilov.neobuzz.R;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VibrationPattern {

    public class GesturePattern {
        private double [] gesture;
        private ArrayList<int[]> pattern;

        public GesturePattern(double[] gesture, ArrayList<int[]> pattern) {
            this.gesture = gesture;
            this.pattern = pattern;
        }
    }
    
    private static final String TAG = "VibrationPattern";
    private String id;
    private List<GesturePattern> patterns = new ArrayList<>();

    public VibrationPattern(Context context) {
        try {
            InputStream jStream = context.getResources().openRawResource(R.raw.pattern_type_1);
            String body = IOUtils.toString(jStream, StandardCharsets.UTF_8);
            jStream.close();
            // json2pojo ??
            JSONObject jsonObject = new JSONObject(body);
            id = jsonObject.getString("type_id");
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
        } catch (IOException | JSONException e) {
            Log.d(TAG, "VibrationPattern: Failed to load vibro pattern" + e.getLocalizedMessage());
        }
    }

    public String getId() {
        return id;
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
