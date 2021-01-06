package com.shatilov.neobuzz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.shatilov.neobuzz.utils.ColourPalette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HandPanel extends View {


    private static final String TAG = "Hand_Canvas";

    private int sizeX, sizeY;

    static final Map<String, Rect> imageRect = new HashMap<>();

    static {
        imageRect.put("palm", new Rect(0, 0, 647, 569));
        imageRect.put("thumb", new Rect(0, 0, 294, 417));
        imageRect.put("thumb_b", new Rect(0, 0, 247, 389));
        imageRect.put("finger", new Rect(0, 0, 162, 734));
    }

    private final Drawable palmImage; // 674x667
    private final Drawable thumbImage; // 294x417
    private final Drawable fingerImage; // 162x734
    private final Drawable thumbBImage; // 162x734

    private final double SF = 0.6;

    private float[] pos = {0, 0, 0, 0, 0};

    public HandPanel(Context context) {
        super(context);

        palmImage = context.getDrawable(R.drawable.palm);
        thumbImage = context.getDrawable(R.drawable.thumb);
        thumbBImage = context.getDrawable(R.drawable.thumb_b);
        fingerImage = context.getDrawable(R.drawable.finger);

    }

    public void setPos(float[] pos) {
        this.pos = pos;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        sizeX = w;
        sizeY = h;
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

    private void drawPalm(Canvas canvas) {
        palmImage.setBounds(getRect("palm"));
        palmImage.draw(canvas);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPalm(canvas);
        drawThumb(canvas);
        drawFingers(canvas);
    }

    private void drawFingers(Canvas canvas) {
        int mx = -17;
        int start = 306;

        int[] dTop = {40, 0, 70, 160};
        int[] dBottom = {0, 0, 30, 50};

        int[] dBTop = {300, 280, 320, 340};
        int[] dBBottom = {120, 150, 120, 80};

        Rect original = getRect("finger");
        int sx = original.width();

        for (int i = 1; i < 5; i++) {
            Rect r = new Rect(original);
            r= placeRect(r, start + (i - 1) * (sx + mx), 50);

            r.bottom += (pos[i] == 1) ? dBBottom[i - 1] : dBottom[i - 1];
            r.top += (pos[i] == 1) ? dBTop[i - 1] : dTop[i - 1];

            fingerImage.setBounds(r);
            fingerImage.draw(canvas);
        }
    }

    private void drawThumb(Canvas canvas) {
        if (pos[0] == 1) {
            Rect r = getRect("thumb_b");
            r = placeRect(r, 175, 365);
            thumbBImage.setBounds(r);
            thumbBImage.draw(canvas);
        } else {
            Rect r = getRect("thumb");
            r = placeRect(r, 90, 365);
            thumbImage.setBounds(r);
            thumbImage.draw(canvas);
        }
    }

}
