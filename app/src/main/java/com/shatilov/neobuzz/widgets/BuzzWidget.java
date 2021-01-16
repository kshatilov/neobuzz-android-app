package com.shatilov.neobuzz.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.shatilov.neobuzz.utils.ColourPalette;

public class BuzzWidget extends Widget {

    private static final float RAD = 20;
    private static final float ITER_STEP = .05F;
    private static final int TARGET_FPS = 10;

    float iter = 1.F;
    private int[] data = {0, 0, 0, 0};
    private final Paint circlePaint;

    public BuzzWidget(Context context) {
        super(context);
        circlePaint = new Paint();
        circlePaint.setColor(ColourPalette.neuralBlue);
        circlePaint.setStyle(Paint.Style.FILL);
        new Thread(() -> {
            while (true) {
                try {
                    iter += ITER_STEP;
                    if (iter >  2.F) {
                        iter = 1.F;
                    }
                    invalidate();
                    Thread.sleep(1000 / TARGET_FPS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void update(int[] data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float pm = sizeX / 4.F;
        float startY = sizeY / 2.F;


        for (int i = 0; i < 4; i++) {
            canvas.drawCircle((i + .5F) * pm, startY, RAD, circlePaint);
            if (data[i] != 0) {
                canvas.drawCircle((i + .5F) * pm, startY, RAD * iter, paint);
            }
        }
    }
}
