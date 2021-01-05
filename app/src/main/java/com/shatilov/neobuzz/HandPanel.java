package com.shatilov.neobuzz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import com.shatilov.neobuzz.utils.ColourPalette;

public class HandPanel extends View {

    private static final int STROKE_WIDTH = 10;

    private int sizeX, sizeY;
    private Paint paint;
    private float SF;

    public HandPanel(Context context) {
        super(context);
        initPaint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        sizeX = w;
        sizeY = h;
        SF = w / 200.F;
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ColourPalette.neuralBlue);
        paint.setStrokeWidth(STROKE_WIDTH);
    }


    private void drawPalm(Canvas canvas) {
        Path palm = new Path();
        palm.moveTo(50 * SF, 100 * SF);
        palm.lineTo(150 * SF, 100 * SF);
        palm.lineTo(150 * SF, 180 * SF);
        palm.lineTo(70 * SF, 180 * SF);
        palm.lineTo(50 * SF, 120 * SF);
        palm.close();
        canvas.drawPath(palm, paint);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPalm(canvas);
        drawThumb(canvas);
        drawFingers(canvas);
    }

    private void drawFingers(Canvas canvas) {
        float startX = 50 * SF;
        float startY = 1 * SF;
        float m = 10 * SF;
        float w = 17 * SF;
        float h = 95 * SF;

        for (int i = 0; i < 4; i++) {
          canvas.drawRect(
                  startX + ((w + m) * i) + w,
                  startY + h,
                  startX + ((w + m) * i),
                  startY,
                  paint
          );
        }
    }

    private void drawThumb(Canvas canvas) {
        Path thumb = new Path();
        thumb.moveTo(20 * SF, 70 * SF);
        thumb.lineTo(45 * SF,120 * SF);
        thumb.lineTo(63 * SF,175 * SF);
        thumb.lineTo(30 * SF,150 * SF);
        thumb.lineTo(10 * SF,70 * SF);
        thumb.close();
        canvas.drawPath(thumb, paint);
    }

}
