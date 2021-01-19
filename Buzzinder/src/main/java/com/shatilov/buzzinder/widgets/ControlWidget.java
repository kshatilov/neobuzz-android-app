package com.shatilov.buzzinder.widgets;

import android.app.Activity;
import android.widget.Button;
import android.widget.LinearLayout;

import com.shatilov.buzzinder.activities.DeckActivity;
import com.shatilov.buzzinder.R;

public class ControlWidget extends LinearLayout {

    private Button yesButton;
    private Button nahButton;

    private static final int M = 10;
    private int sizeY;
    private int sizeX;

    public ControlWidget(Activity activity) {
        super(activity);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        yesButton = new Button(getContext());
        nahButton = new Button(getContext());

        yesButton.setBackgroundResource(R.drawable.yes_icon);
        nahButton.setBackgroundResource(R.drawable.nah_icon);

        if (activity instanceof DeckActivity) {
            yesButton.setOnClickListener((b) -> {
               ((DeckActivity)activity).swipe(true);
            });
            nahButton.setOnClickListener((b) -> {
                ((DeckActivity)activity).swipe(false);
            });
        }


        addView(yesButton);
        addView(nahButton);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        sizeY = bottom - top;
        sizeX = right - left;

        nahButton.layout(sizeX / 2 - sizeY - M, 0, sizeX / 2 - M, sizeY);
        yesButton.layout(sizeX / 2 + M, 0, sizeX / 2 + sizeY + M, sizeY);
    }
}
