package hk.ust.shatilov.myokey3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.shatilov.neobuzz.common.widgets.Widget;

public class ClfWidget extends Widget {

    private String label = "palm";

    public ClfWidget(Context context) {
        super(context);
    }

    public void update(String label) {
        this.label = label;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int startY = sizeY / 2;
        int midX = sizeX / 2;
        int dx = 50;
        int dy = 200;
        canvas.drawLine(0, startY, midX - dx, startY, paint);
        canvas.drawLine(midX + dx, startY, sizeX, startY, paint);

        if ("palm".equals(label)) {
            canvas.drawLine(midX - dx, startY, midX + dx, startY, paint);
        } else if ("top".equals(label)) {
            canvas.drawLine(midX - dx, startY , midX, startY - dy, paint);
            canvas.drawLine(midX, startY - dy, midX + dx, startY, paint);
        } else if ("bot".equals(label)) {
            canvas.drawLine(midX - dx, startY , midX, startY + dy, paint);
            canvas.drawLine(midX, startY + dy, midX + dx, startY, paint);
        } else if ("mid".equals(label)) {
            canvas.drawCircle(midX, startY, dx, paint);
        } else if ("fist".equals(label)) {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(midX, startY, dx, paint);
            paint.setStyle(Paint.Style.STROKE);
        }
    }
}

