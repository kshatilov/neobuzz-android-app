package com.shatilov.buzzinder;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ScoreWidget extends LinearLayout {
    private final TextView scoreLabel;
    private final ImageView icon;
    private final TextView label;

    protected int sizeX;
    protected int sizeY;
    public static final int M = 100;
    public static final int FS = 28;

    private int currentScore = 0;
    private int totalScore = 0;

    public ScoreWidget(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(TEXT_ALIGNMENT_CENTER);
        setPadding(M, 30, 0, 0);

        icon = new ImageView(getContext());
        icon.setImageDrawable(getContext().getDrawable(R.drawable.main_icon));
        addView(icon);

        label = new TextView(getContext());
        label.setTextSize(FS);
        label.setTypeface(null, Typeface.BOLD);
        label.setText("buzzinder");
        addView(label);

        scoreLabel = new TextView(getContext());
        scoreLabel.setTextSize(FS);
        scoreLabel.setText(currentScore + "/" + totalScore);
        scoreLabel.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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
        sizeY = bottom - top;
        sizeX = right - left;
        icon.requestLayout();
        icon.getLayoutParams().height = 120;
        icon.getLayoutParams().width = 120;
        label.layout(left + 250, top + 35, left + M * 7, bottom);
        scoreLabel.layout(left + 750, top + 35, left + M * 9, bottom);
    }
}
