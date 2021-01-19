package com.shatilov.neobuzz.common.haptics;

import android.os.Parcelable;

import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.utils.BuzzWrapper;

public abstract class HapticTranslator {

    protected BuzzWrapper buzz;
    protected Hand hand;

    public HapticTranslator(Hand hand, BuzzWrapper buzz) {
        this.hand = hand;
        this.buzz = buzz;
    }

    public void setBuzz(BuzzWrapper buzz) {
        this.buzz = buzz;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    abstract public void vibrate();
}
