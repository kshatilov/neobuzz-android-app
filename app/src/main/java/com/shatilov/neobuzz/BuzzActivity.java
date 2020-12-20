package com.shatilov.neobuzz;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.shatilov.neobuzz.utils.BuzzAwareActivity;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.utils.ColourPalette;

import java.util.ArrayList;

public class BuzzActivity extends AppCompatActivity implements BuzzAwareActivity {

    private static final String TAG = "Buzz Activity";
    ArrayList<SeekBar> seekBars;

    ArrayList<Integer> motorSwipes;
    Button buzzConnectButton;
    int swipeOrder = 0;

    BuzzWrapper buzz = new BuzzWrapper(this);
    int[] motorIntensities = new int[4];
    private Button clearButton;

    private SwipePanel swipePanel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buzz_activity);
        unitUI();
        for (int i = 0; i < 4; ++i) {
            motorIntensities[i] = 0;
        }
    }

    private void initMotorElements(String id, ArrayList<? extends View> container) {
        for (int i = 0; i < 4; ++i) {
            int resID = getResources().getIdentifier(id + (i),
                    "id", getPackageName());
            container.add(i, findViewById(resID));
        }
    }

    private void unitUI() {

        swipePanel = new SwipePanel(this);

        // connect button
        buzzConnectButton = findViewById(R.id.buzz_connect_button);
        buzzConnectButton.setOnClickListener((button) -> {
            buzz.init();
            buzzConnectButton.setEnabled(false);
            buzzConnectButton.setText("Connecting...");
        });

        // seekBars
        seekBars = new ArrayList<>(4);
        initMotorElements("seekBar", seekBars);

        for (SeekBar seekBar : seekBars) {
            seekBar.setMax(buzz.MAX_VIBRATION);
            seekBar.getProgressDrawable().setColorFilter(ColourPalette.neuralBlue, PorterDuff.Mode.SRC_IN);
            seekBar.getThumb().setColorFilter(ColourPalette.neuralBlue, PorterDuff.Mode.SRC_IN);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                    String name = seekBar.getResources().getResourceName(seekBar.getId());
                    int seekBarIndex = Integer.parseInt(name.substring(name.length() - 1));
                    motorIntensities[seekBarIndex] = value;
                    buzz.sendVibration(motorIntensities);
                    clearButton.setEnabled(true);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        // swipe motor buttons
        motorSwipes = new ArrayList<>(4);

        clearButton = findViewById(R.id.clear_swipe_button);
        clearButton.setOnClickListener(button -> {
            swipeOrder = 0;
            buzz.stopVibration();
            seekBars.forEach(e -> ((SeekBar) e).setProgress(0));
            swipePanel.clearPaths();
        });

        motorSwipes.forEach(e -> e = 0);

        LinearLayout container = findViewById(R.id.container);
        container.addView(swipePanel);
    }


    @Override
    public void handleConnect(int code) {
        if (code > 0) {
            buzzConnectButton.setText("Connected");
            buzzConnectButton.setEnabled(false);
            clearButton.setVisibility(View.VISIBLE);
        }
    }

    public void swipePanelCallback() {
        swipePanel.getPaths().forEach((ArrayList<Integer> path) -> {
                    buzz.sendSwipe(path.toArray());
                }
        );
    }

    public void motorSwipeCallback(int[] motorsState) {
        for (int i = 0; i < 4; ++i) {
            seekBars.get(i).setProgress(motorsState[i]);
        }
    }
}
