package com.shatilov.neobuzz.widgets;

import android.content.Context;
import android.graphics.Canvas;

public class MyoWidget extends Widget {
    private static final int TARGET_FPS = 10;
    private static final int MAX_FPS = 200;

    long iter;
    private float[] data = {0.F, 0.F, 0.F, 0.F, 0.F, 0.F, 0.F, 0.F};

    public MyoWidget(Context context) {
        super(context);
        iter = 0;
    }

    public void update(float[] data) {
        if (iter % (MAX_FPS / TARGET_FPS) == 0) {
            this.data = data;
            invalidate();
        }
        ++iter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int pc = sizeX / 8;
        int maxY = sizeY / 2;
        int startY = sizeY / 2;
        int maxEmg = 128;
        for (int i = 0; i < 8; i++) {
            float y = startY - maxY * data[i] / maxEmg;
            canvas.drawLine(i * pc, startY,  (i + .5F) * pc, y, paint);
            canvas.drawLine((i + .5F) * pc, y, (i + 1) * pc, startY, paint);
        }
    }
}
