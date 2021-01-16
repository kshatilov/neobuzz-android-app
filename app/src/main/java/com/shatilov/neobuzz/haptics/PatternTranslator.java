package com.shatilov.neobuzz.haptics;

import android.content.Context;
import android.util.Log;

import com.shatilov.neobuzz.Hand;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.widgets.BuzzWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* The idea is to identify several key states and assign distinctive patterns
* to them, similar to the training examples in the original NeoBuzz App
* */
public class PatternTranslator extends VibroTranslator {
    private static final String TAG = "PatternTranslator";

    public PatternTranslator(Context context, Hand hand, BuzzWrapper buzz) {
        super(context, hand, buzz);
    }


    @Override
    public void vibrate() {
        buzz.stopVibration();
        List<int[]> vibrations = vibrationPattern.getPattern(hand.getFingerPositions());
        if (null == vibrations) {
            Log.d(TAG, "pattern for the current state is not provided");
            buzz.stopVibration();
        }
        if (buzz.isConnected()) buzz.sendVibration(vibrations);
    }
}
