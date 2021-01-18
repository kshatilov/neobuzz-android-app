package com.shatilov.buzzinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.shatilov.neobuzz.common.Hand;

public class MainActivity extends AppCompatActivity {

    private Hand hand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}