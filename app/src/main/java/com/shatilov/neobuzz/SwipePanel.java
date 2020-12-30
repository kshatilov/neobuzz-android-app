package com.shatilov.neobuzz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.shatilov.neobuzz.utils.ColourPalette;

import java.util.ArrayList;

public class SwipePanel extends View {
    private static final String TAG = "SwipePanel";
    private static final int RAD = 19;
    private static final int TH = 10;

    private float touchX, touchY;
    private int sizeX, sizeY;
    boolean toAdd = true;

    private final ArrayList<Pair<Integer, Integer>> centers = new ArrayList<>(4);
    private ArrayList<Integer> activePath = new ArrayList<>();
    private final ArrayList<ArrayList<Integer>> paths = new ArrayList<>(2);

    public SwipePanel(Context context) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setOnTouchListener((View v,  MotionEvent event) -> {
            performClick();
            float screenX = event.getX();
            float screenY = event.getY();
            float viewX = screenX - v.getLeft();
            float viewY = screenY - v.getTop();
            touchX = viewX;
            touchY = viewY;
            int activeZone = (int) (touchX / (sizeX / 4));

            if (!activePath.contains(activeZone)) {
                activePath.add(activeZone);
                if (toAdd) {
                    paths.add(activePath);
                    toAdd = false;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                activePath = new ArrayList<>();
                toAdd = true;
                if (context instanceof BuzzActivity) {
                    ((BuzzActivity) context).swipePanelCallback();
                }
            }
            invalidate();
            return true;
        });
        initPaint();
    }

    Paint inactivePaint, activePaint;

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        sizeX = w;
        sizeY = h;
        initCircles();
    }

    private void initPaint() {
        inactivePaint = new Paint();
        inactivePaint.setAntiAlias(true);
        inactivePaint.setStyle(Paint.Style.STROKE);
        inactivePaint.setColor(Color.GRAY);
        inactivePaint.setStrokeWidth(TH);
        inactivePaint.setStyle(Paint.Style.FILL);


        activePaint = new Paint();
        activePaint.setAntiAlias(true);
        activePaint.setStyle(Paint.Style.STROKE);
        activePaint.setColor(ColourPalette.neuralBlue);
        activePaint.setStrokeWidth(TH);
        activePaint.setStyle(Paint.Style.FILL);
    }

    private void initCircles() {
        for (int i = 0; i < 4; ++i) {
            int step = sizeX / 4;
            int cx = step * i + step / 2;
            int cy = sizeY / 2;
            centers.add(new Pair<>(cx, cy));
        }
    }

    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < 4; ++i) {
            boolean isActive = false;

            for (ArrayList<Integer> path: paths) {
                if (path.contains(i)) {
                    isActive = true;
                    break;
                }
            }

            Pair<Integer, Integer> center = centers.get(i);
            canvas.drawCircle(center.first, center.second, RAD, isActive ? activePaint : inactivePaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircles(canvas);
        drawTouchLine(canvas);
        drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {

        activePaint.setAlpha(80);
        activePaint.setStrokeWidth(6);
        paths.forEach(path ->
        {
            for (int i = 0; i < path.size() - 1; ++i) {
                Pair<Integer, Integer> start = centers.get(path.get(i));
                int index = path.get(i + 1);
                index = Math.min(3, Math.max(0, index));
                Pair<Integer, Integer> end = centers.get(index);
                canvas.drawLine(start.first, start.second, end.first, end.second, activePaint);
                if (i == path.size() - 2) {
                    int arrowX = end.first + 45 * (path.get(i) - path.get(i + 1));
                    int arrowY = end.second + 5;
                    int arrowY_ = end.second - 5;
                    canvas.drawLine(arrowX, arrowY, end.first, end.second, activePaint);
                    canvas.drawLine(arrowX, arrowY_, end.first, end.second, activePaint);
                }
            }
        });
        activePaint.setAlpha(255);
        activePaint.setStrokeWidth(TH);
    }

    public void clearPaths() {
        paths.clear();
        invalidate();
    }

    public ArrayList<ArrayList<Integer>> getPaths() {
        return paths;
    }

    private void drawTouchLine(Canvas canvas) {
        int size = activePath.size();

        if (size < 1) {
            return;
        }

        int index = activePath.get(size - 1);
        index = Math.min(3, Math.max(0, index));

        Pair<Integer, Integer> activeCenter = centers.get(index);

        float startX = activeCenter.first;
        float startY = activeCenter.second;

        canvas.drawLine(startX, startY, touchX, touchY, activePaint);
    }
}
