package com.shatilov.neobuzz.haptics;

import com.shatilov.neobuzz.Hand;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.utils.MyoWrapper;

import java.util.HashMap;
import java.util.Map;

public class NaiveTranslator implements VibroTranslator {
    private final Hand hand;
    private final BuzzWrapper buzz;
    private final MyoWrapper myo;

    private static final Map<Double, VibrationIntensity> state2vibration = new HashMap<>();

    static {
        state2vibration.put(0., VibrationIntensity.NONE);
        state2vibration.put(.5, VibrationIntensity.MEDIUM);
        state2vibration.put(1., VibrationIntensity.HIGH);
    }

    public NaiveTranslator(Hand hand, BuzzWrapper buzz, MyoWrapper myo) {
        this.hand = hand;
        this.buzz = buzz;
        this.myo = myo;
    }

    @Override
    public int[] vibrate() {
        double[] states = hand.getFingerPositions();

        // haptic feedback for finger 0 on Myo
        if (myo.isConnected()) {
            myo.sendPersistentVibration(
                    state2vibration.get(states[0])
            );
        }

        // haptic feedback for fingers 1-4 on Buzz
        int[] buzzIntensities = {0, 0, 0, 0};
        if (!buzz.isConnected()) {
            return buzzIntensities;
        }
        for (int i = 1; i < 5; i++) {
            buzzIntensities[i-1] = state2vibration.get(states[i]).getValue();
        }
        buzz.sendVibration(buzzIntensities);
        return buzzIntensities;
    }
}
