package com.shatilov.neobuzz.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.MotionEvent;

import com.shatilov.neobuzz.common.utils.ColourPalette;
import com.shatilov.neobuzz.common.utils.HapticFeedbackActivity;
import com.shatilov.neobuzz.common.widgets.Widget;

import java.util.HashMap;
import java.util.Map;

public class HandPanel extends Widget {


    private static final String TAG = "Hand_Canvas";
    private final Context context;


    private static final int shiftX = -40;
    private static final int RAD = 30;
    private static final Map<String, Rect> imageRect = new HashMap<>();
    static final Map<Double, Double> transitions = new HashMap<>();
    /* finger position to top and bottom edges of the finger image */
    static final Map<Double, Pair<int[], int[]>> drawPositions = new HashMap<>();

    static {
        imageRect.put("palm", new Rect(0, 0, 647, 569));
        imageRect.put("thumb", new Rect(0, 0, 294, 417));
        imageRect.put("thumb_b", new Rect(0, 0, 247, 389));
        imageRect.put("finger", new Rect(0, 0, 162, 734));

        transitions.put(0., .5);
        transitions.put(.5, 1.);
        transitions.put(1., 0.);

        drawPositions.put(0., new Pair<>(new int[]{40, 0, 70, 160}, new int[]{0, 0, 30, 50}));
        drawPositions.put(0.5, new Pair<>(new int[]{130, 140, 130, 250}, new int[]{0, 0, 30, 50}));
        drawPositions.put(1., new Pair<>(new int[]{330, 310, 350, 390}, new int[]{120, 150, 120, 80}));
    }

    private final Drawable palmImage; // 674x667
    private final Drawable thumbImage; // 294x417
    private final Drawable fingerImage; // 162x734
    private final Drawable thumbBImage; // 162x734
    private final int[] touch2Finger = {0, 0, 0, 0, 0};

    private boolean isTouchEnabled = true;
    private Hand hand;

    private final double SF = 0.66;

    public HandPanel(Context context, Hand hand) {
        super(context);

        this.context = context;
        this.hand = hand;

        palmImage = context.getDrawable(R.drawable.palm);
        thumbImage = context.getDrawable(R.drawable.thumb);
        thumbBImage = context.getDrawable(R.drawable.thumb_b);
        fingerImage = context.getDrawable(R.drawable.finger);

    }

    public void setTouchEnabled(boolean touchEnabled) {
        isTouchEnabled = touchEnabled;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
        invalidate();
    }

    public void update() {
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        if (!isTouchEnabled) {
            return super.onTouchEvent(event);
        }

        float x = event.getX();
        int index = 0;

        for (int i = 0; i < 5; i++) {
            if (x > touch2Finger[i]) {
                index = i;
            }
        }

        if (0 != index) {
            hand.bendFinger(index, transitions.get(hand.getFingerPositions()[index]));
        } else {
            hand.bendFinger(index,
                    Double.compare(hand.getFingerPositions()[index], 0) == 0 ? 1 : 0);
        }

        invalidate();

        if (context instanceof HapticFeedbackActivity) {
            ((HapticFeedbackActivity)context).onHandUpdated();
        }

        return super.onTouchEvent(event);
    }

    /**
     * Pun intended
     */
    private Rect getRect(String name) {
        Rect r = new Rect(imageRect.get(name));

        int left = (int) ((sizeX - r.width() * SF) / 2);
        int top = (int) ((sizeY - r.height() * SF) / 2);
        int w = (int) (left + r.width() * SF);
        int h = (int) (top + r.height() * SF);

        r.set(left, top, w, h);
        return new Rect(r);
    }

    private Rect placeRect(Rect r, int x, int y) {
        int w = x + r.width();
        int h = y + r.height();

        r.set(x, y, w, h);

        return new Rect(r);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPalm(canvas);
        drawThumb(canvas);
        drawFingers(canvas);
        drawSensors(canvas);
    }

    private void drawPalm(Canvas canvas) {
        Rect r = getRect("palm");
        r = placeRect(r, (int) (424 * SF) + shiftX, (int) (700 * SF));
        palmImage.setBounds(r);
        palmImage.draw(canvas);
    }

    private void drawFingers(Canvas canvas) {
        int mx = (int) (-29 * SF);
        int startX = (int) (510 * SF);
        int startY = (int) (50 * SF);

        Rect original = getRect("finger");
        int sx = original.width();
        touch2Finger[0] = 0;

        for (int i = 0; i < 4; i++) {
            Rect r = new Rect(original);
            int x = startX + (i) * (sx + mx) + shiftX;
            touch2Finger[i + 1] = x;
            r = placeRect(r, x, startY);

            Pair<int[], int[]> config = drawPositions.get(hand.getFingerPositions()[i + 1]);
            r.top += config.first[i] * SF;
            r.bottom += config.second[i] * SF;

            fingerImage.setBounds(r);
            fingerImage.draw(canvas);
        }
    }

    private void drawThumb(Canvas canvas) {
        if (Double.compare(hand.getFingerPositions()[0], 1.) == 0) {
            Rect r = getRect("thumb_b");
            r = placeRect(r, (int) (290 * SF) + shiftX, (int) (610 * SF));
            thumbBImage.setBounds(r);
            thumbBImage.draw(canvas);
        } else {
            Rect r = getRect("thumb");
            r = placeRect(r, (int) (150 * SF) + shiftX, (int) (590 * SF));
            thumbImage.setBounds(r);
            thumbImage.draw(canvas);
        }
    }

    private void drawPressureCircle(int x, int y, int index, Canvas canvas) {
        if (hand.getPressure()[index] > 0) {
            paint.setColor(ColourPalette.pointyRed);
        }
        canvas.drawCircle(x, y, RAD, paint);
        paint.setColor(Color.LTGRAY);

    }

    private void drawSensors(Canvas canvas) {
        // thumb sensor
        int x, y;
        int mX = (int) (80 * SF);
        int mY = (int) (170 * SF);
        if (Double.compare(hand.getFingerPositions()[0], 1.) == 0) {
            x = (int) (350 * SF) + shiftX + mX;
            y = (int) (540 * SF) + mY;
            canvas.drawCircle(x, y, RAD, paint);
        } else {
            x = (int) ((155 * SF) + shiftX) + mX;
            y = (int) (500 * SF) + mY;
        }

        drawPressureCircle(x, y, 0, canvas);

        // palm sensor
        x = (int) (700 * SF) + shiftX;
        y = (int) (1000 * SF);
        drawPressureCircle(x, y, 1, canvas);


        // index sensor
        x = touch2Finger[1] + mX;
        y = (int) (drawPositions.get(hand.getFingerPositions()[1]).first[1] * SF) + mY;
        drawPressureCircle(x, y, 2, canvas);

    }
}
