package com.shatilov.neobuzz.common.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;


public abstract class Widget extends View {

    protected Paint paint;
    protected int sizeX;
    protected int sizeY;

    public Widget(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initPaint();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        sizeX = w;
        sizeY = h;
    }

}
