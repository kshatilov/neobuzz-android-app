package com.shatilov.buzzinder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shatilov.buzzinder.R;
import com.shatilov.buzzinder.widgets.LogoWidget;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        LinearLayout logoContainer = findViewById(R.id.logo_container);
        logoContainer.addView(new LogoWidget(getApplicationContext()));

        String score = getIntent().getExtras().getString("SCORE");
        TextView scoreDisplay = findViewById(R.id.score_display);
        scoreDisplay.setText(score);

        Button again = findViewById(R.id.again);
        again.setOnClickListener((b) -> {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        });
    }
}