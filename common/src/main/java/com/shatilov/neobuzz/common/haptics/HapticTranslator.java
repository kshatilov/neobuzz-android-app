package com.shatilov.neobuzz.common.haptics;

import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.utils.BuzzWrapper;

public abstract class HapticTranslator {

    protected final BuzzWrapper buzz;
    protected Hand hand;

    public HapticTranslator(Hand hand, BuzzWrapper buzz) {
        this.hand = hand;
        this.buzz = buzz;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    abstract public void vibrate();
}
