package com.shatilov.neobuzz.haptics;

import com.shatilov.neobuzz.Hand;
import com.shatilov.neobuzz.utils.BuzzWrapper;

public abstract class HapticTranslator {

    protected final BuzzWrapper buzz;
    protected final Hand hand;

    public HapticTranslator(Hand hand, BuzzWrapper buzz) {
        this.hand = hand;
        this.buzz = buzz;
    }

    abstract public void vibrate();
}
