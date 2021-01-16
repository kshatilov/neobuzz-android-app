package com.shatilov.neobuzz.haptics;

import android.content.Context;

import com.shatilov.neobuzz.Hand;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.utils.MyoWrapper;
import com.shatilov.neobuzz.widgets.BuzzWidget;

import java.util.HashMap;
import java.util.Map;

public class NaiveTranslator extends VibroTranslator {
    private MyoWrapper myo;

    private static final Map<Double, VibrationIntensity> state2vibration = new HashMap<>();

    static {
        state2vibration.put(0., VibrationIntensity.NONE);
        state2vibration.put(.5, VibrationIntensity.MEDIUM);
        state2vibration.put(1., VibrationIntensity.HIGH);
    }

    public NaiveTranslator(Context context, Hand hand, BuzzWrapper buzz) {
        super(context, hand, buzz);
    }

    public void setMyo(MyoWrapper myo) {
        this.myo = myo;
    }

    @Override
    public void vibrate() {
        double[] states = hand.getFingerPositions();

        // haptic feedback for finger 0 on Myo
        if (null != myo && myo.isConnected()) {
            myo.sendPersistentVibration(
                    state2vibration.get(states[0])
            );
        }

        // haptic feedback for fingers 1-4 on Buzz
        int[] buzzIntensities = {0, 0, 0, 0};
        for (int i = 1; i < 5; i++) {
            buzzIntensities[i-1] = state2vibration.get(states[i]).getValue();
        }
        buzz.sendVibration(buzzIntensities);
    }
}
