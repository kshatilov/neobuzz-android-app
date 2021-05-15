package com.shatilov.neobuzz.common.haptics;

import android.content.Context;

import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.utils.BuzzEncoder;
import com.shatilov.neobuzz.common.utils.BuzzWrapper;

public class AutoencTranslator extends HapticTranslator {
    private BuzzEncoder encoder;
    private Context context;


    public AutoencTranslator(Hand hand, BuzzWrapper buzz) {
        super(hand, buzz);
    }

    public void setContext(Context context) {
        encoder = new BuzzEncoder(context);
    }

    @Override
    public void vibrate() {
        float[] input = new float[140];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 5; ++j) {
                input[i * 7 + j] = (float) hand.getFingerPositions()[j];
            }
            input[i * 7 + 5] = (float) hand.getPressure()[2];
            input[i * 7 + 6] = (float) hand.getPressure()[1];
        }
        if (null != encoder) {
            int[] res = encoder.predict(input);
            if (buzz != null && buzz.isConnected()) {
                buzz.sendVibration(res);
            }
        }


    }
}
