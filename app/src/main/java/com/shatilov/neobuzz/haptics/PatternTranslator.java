package com.shatilov.neobuzz.haptics;

import android.util.Log;

import com.shatilov.neobuzz.Hand;
import com.shatilov.neobuzz.utils.BuzzWrapper;

import java.util.List;

/**
* The idea is to identify several key states and assign distinctive patterns
* to them, similar to the training examples in the original NeoBuzz App
* */
public class PatternTranslator extends HapticTranslator {
    private static final String TAG = "PatternTranslator";

    HapticProfile hapticProfile;

    public PatternTranslator(Hand hand, BuzzWrapper buzz) {
        super(hand, buzz);
    }

    public void setHapticProfile(HapticProfile hapticProfile) {
        this.hapticProfile = hapticProfile;
    }

    @Override
    public void vibrate() {
        buzz.stopVibration();
        if (null == hapticProfile) {
            return;
        }
        List<int[]> vibrations = hapticProfile.getPattern(hand.getFingerPositions());
        if (null == vibrations) {
            Log.d(TAG, "pattern for the current state is not provided");
            buzz.stopVibration();
        }
        if (buzz.isConnected()) {
            buzz.setInterval(hapticProfile.getInterval());
            buzz.sendVibration(vibrations);
        }
    }
}
