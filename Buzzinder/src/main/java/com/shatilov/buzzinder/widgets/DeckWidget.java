package com.shatilov.buzzinder.widgets;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.shatilov.buzzinder.activities.DeckActivity;
import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.HandPanel;
import com.shatilov.neobuzz.common.widgets.BuzzWidget;


public class DeckWidget extends LinearLayout {

    public static final int R = 40;
    public static final int M = 100;
    public static final int DX = 250;
    public static final String TAG = "Deck";

    protected Paint paint;
    protected int sizeX;
    protected int sizeY;
    private RectF rect;
    private float degree = 0;
    private float startX = 0;

    private final BuzzWidget buzzWidget;
    private final HandPanel handPanel;

    public DeckWidget(Activity activity, BuzzWidget buzzWidget) {
        super(activity);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        initPaint();
        rect = new RectF();

        this.buzzWidget = buzzWidget;

        handPanel = new HandPanel(getContext(), new Hand());
        handPanel.setTouchEnabled(false);

        setPadding(M, 0, M, 0);
        setOrientation(LinearLayout.VERTICAL);

        addView(this.buzzWidget);
        addView(handPanel);

        setOnTouchListener((v, event) -> {
            v.performClick();
            float x = event.getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = x;
                    break;
                case MotionEvent.ACTION_MOVE:
                    degree = (float) Math.atan((startX - x) / sizeY);
                    degree = (float) ((degree * 180) / Math.PI);
                    setRotation(-degree);
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(startX - x) > DX) {
                        if (activity instanceof DeckActivity) {
                            boolean right = startX < x;
                            ((DeckActivity) activity).swipe(right);
                        }
                    }
                    setRotation(0);
                    break;
            }
            invalidate();
            return true;
        });
    }

    public void setHand(Hand hand) {
        handPanel.setHand(hand);
        handPanel.invalidate();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        sizeY = bottom - top;
        sizeX = right - left;
        rect.set(M, M, sizeX - M, sizeY - M);
        setPivotX(sizeX / 2.F);
        setPivotY(sizeY);
        handPanel.layout(left + M, top + M, right - M, bottom - 2 * M);
        buzzWidget.layout(left + 2 * M, top + handPanel.getHeight() - M, right - 2 * M, bottom - M);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawRoundRect(rect, R, R, paint);
    }
}
