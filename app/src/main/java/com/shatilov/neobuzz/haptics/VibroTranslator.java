package com.shatilov.neobuzz.haptics;

import android.content.Context;

import com.shatilov.neobuzz.Hand;
import com.shatilov.neobuzz.utils.BuzzWrapper;

public abstract class VibroTranslator {

    protected final VibrationPattern vibrationPattern;
    protected final BuzzWrapper buzz;
    protected final Hand hand;

    public VibroTranslator(Context context, Hand hand, BuzzWrapper buzz) {
        vibrationPattern = new VibrationPattern(context);
        this.hand = hand;
        this.buzz = buzz;
    }

    abstract public void vibrate();
}
