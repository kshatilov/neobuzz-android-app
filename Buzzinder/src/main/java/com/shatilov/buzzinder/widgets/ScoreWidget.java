package com.shatilov.buzzinder.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shatilov.buzzinder.R;
import com.shatilov.neobuzz.common.utils.ColourPalette;


public class ScoreWidget extends LinearLayout {
    private final TextView scoreLabel;

    public static final int M = 100;
    public static final int FS = 28;

    private int currentScore = 0;
    private int totalScore = 0;

    public ScoreWidget(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(TEXT_ALIGNMENT_CENTER);
        setPadding(M, 30, 0, 0);

        addView(new LogoWidget(context));

        scoreLabel = new TextView(getContext());
        scoreLabel.setTextSize(FS);
        scoreLabel.setText(currentScore + "/" + totalScore);
        scoreLabel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        scoreLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        addView(scoreLabel);
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
        scoreLabel.setText(currentScore + "/" + totalScore);
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
        scoreLabel.setText(currentScore + "/" + totalScore);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        scoreLabel.layout(left + 8 * M, top + 35, left + 10 * M, bottom);
    }
}
