package com.shatilov.buzzinder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.shatilov.buzzinder.R;
import com.shatilov.buzzinder.widgets.LogoWidget;
import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.haptics.HapticTranslator;
import com.shatilov.neobuzz.common.haptics.NaiveTranslator;
import com.shatilov.neobuzz.common.utils.BuzzWrapper;
import com.shatilov.neobuzz.common.utils.ColourPalette;
import com.shatilov.neobuzz.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    public static final String SELECTION = "haptic_selection";

    private String selection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        LinearLayout logoContainer = findViewById(R.id.logo_container);
        logoContainer.addView(new LogoWidget(getApplicationContext()));

        Map<String, HapticTranslator> hapticOptions = Utils.initHapticConfig(new Hand(), new BuzzWrapper(this, null));
        if (null == hapticOptions) {
            return;
        }

        hapticOptions.put("Naive", null);
        Spinner typeSelector = findViewById(R.id.type_spinner);
        List<String> optionList = new ArrayList<>(hapticOptions.keySet());
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, optionList);
        typeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = ((TextView)parent.getChildAt(0));
                tv.setTextColor(ColourPalette.ultramaGreen);
                tv.setTextSize(25);
                tv.setTypeface(null, Typeface.BOLD);
                selection = optionList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        typeSelector.setAdapter(adapter);
        Button go = findViewById(R.id.go);
        go.setOnClickListener((b) -> {
            Intent intent = new Intent(this, BuzzinderActivity.class);
            intent.putExtra(SELECTION, selection);
            startActivity(intent);
        });
    }
}