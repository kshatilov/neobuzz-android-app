package com.shatilov.buzzinder.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shatilov.buzzinder.R;
import com.shatilov.neobuzz.common.utils.ColourPalette;


public class LogoWidget extends LinearLayout {

    private final ImageView icon;
    private final TextView label;

    public LogoWidget(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        icon = new ImageView(context);
        icon.setImageDrawable(context.getTheme().getDrawable(R.drawable.main_icon));
        addView(icon);

        label = new TextView(context);
        label.setTextSize(28);
        label.setTypeface(null, Typeface.BOLD);
        label.setText(R.string.app_name);
        label.setTextColor(ColourPalette.pointyRed);
        addView(label);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            icon.requestLayout();
        }
        icon.getLayoutParams().height = 100;
        icon.getLayoutParams().width = 100;
        label.layout(100, 0, 710, bottom);
    }

}
