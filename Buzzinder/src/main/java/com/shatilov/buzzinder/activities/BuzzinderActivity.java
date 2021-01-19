package com.shatilov.buzzinder.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.shatilov.buzzinder.R;
import com.shatilov.buzzinder.widgets.ControlWidget;
import com.shatilov.buzzinder.widgets.DeckWidget;
import com.shatilov.buzzinder.widgets.ScoreWidget;
import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.haptics.HapticTranslator;
import com.shatilov.neobuzz.common.haptics.NaiveTranslator;
import com.shatilov.neobuzz.common.utils.BuzzAwareActivity;
import com.shatilov.neobuzz.common.utils.BuzzWrapper;
import com.shatilov.neobuzz.common.utils.ColourPalette;
import com.shatilov.neobuzz.common.widgets.BuzzWidget;

import java.util.ArrayList;
import java.util.List;

public class BuzzinderActivity extends AppCompatActivity implements BuzzAwareActivity, DeckActivity {

    private BuzzWrapper buzz;
    private HapticTranslator vibrator;

    private List<Hand> deck;
    private int deckPointer = 0;
    private boolean impostor = false;
    private int score = 0;

    private BuzzWidget buzzWidget;
    private ScoreWidget scoreWidget;
    private ControlWidget controlWidget;
    private DeckWidget deckWidget;
    private PopupWindow popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNaiveDeck();
        buzzWidget = new BuzzWidget(getApplicationContext());
        buzz = new BuzzWrapper(this, buzzWidget);
        buzz.connect();
        vibrator = new NaiveTranslator(new Hand(), buzz);

        LinearLayout deckContainer = findViewById(R.id.deck_container);
        deckWidget = new DeckWidget(this, buzzWidget);
        deckContainer.addView(deckWidget);
        deckContainer.setOnClickListener((e) -> popup.dismiss());

        LinearLayout scoreContainer = findViewById(R.id.score_container);
        scoreWidget = new ScoreWidget(this);
        scoreContainer.addView(scoreWidget);

        LinearLayout controlContainer = findViewById(R.id.control_container);
        controlWidget = new ControlWidget(this);
        controlContainer.addView(controlWidget);

        scoreWidget.setTotalScore(deck.size());
        scoreWidget.setCurrentScore(score);

        deckWidget.setHand(deck.get(0));
        vibrator.setHand(deck.get(0));
        vibrator.vibrate();

        String selection = getIntent().getExtras().getString(StartActivity.SELECTION);
        Log.d("TAG", "onCreate: " + selection);

        initPopup();
    }

    private void initPopup() {
        popup = new PopupWindow(this);
        LinearLayout layout = new LinearLayout(this);
        TextView result = new TextView(this);
        result.setTypeface(null, Typeface.BOLD_ITALIC);
        result.setTextSize(50);
        result.setTextColor(ColourPalette.pointyRed);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        result.setText(getResources().getText(R.string.it_s_a_match));
        result.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        layout.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(result, params);

        popup.setContentView(layout);
        popup.setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.TRANSPARENT));
        layout.setOnClickListener((e) -> popup.dismiss());
        result.setOnClickListener((e) -> popup.dismiss());
    }

    private void initNaiveDeck() {
        deck = new ArrayList<>();
        deck.add(new Hand(new double[]{0, 0, 0, 0, 1}));
        deck.add(new Hand(new double[]{0, 0, 0, 1, 0}));
        deck.add(new Hand(new double[]{0, 0, 1, 0, 0}));
        deck.add(new Hand(new double[]{0, 1, 0, 0, 0}));
        deck.add(new Hand(new double[]{0, 1, 1, 0, 0}));
        deck.add(new Hand(new double[]{0, 1, 1, 1, 0}));
        deck.add(new Hand(new double[]{0, 1, 1, 1, 1}));
        // shuffle
        // add impostors
    }

    @Override
    public void onBuzzConnect(int code) {
        if (code >= 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Buzz is connected", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            vibrator.vibrate();
        }
    }

    public void showResult(boolean isCorrect) {
        if (isCorrect) {
            popup.showAtLocation(findViewById(R.id.main_container), Gravity.CENTER, 0, 0);
            popup.update(0, 300, 1000, 1000);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getResources().getText(R.string.nope), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public boolean swipe(boolean right) {
        popup.dismiss();
        boolean isCorrect = !impostor && right;
        if (isCorrect) {
            scoreWidget.setCurrentScore(++score);
        }
        showResult(isCorrect);
        if (deckPointer < deck.size() - 1) {
            Hand hand = deck.get(++deckPointer);
            deckWidget.setHand(hand);
            vibrator.setHand(hand);
            vibrator.vibrate();
        } else {
            // display score and go back to selection
            Intent intent = new Intent(this, ScoreActivity.class);
            intent.putExtra("SCORE", score + " out of " + deck.size());
            startActivity(intent);
        }
        return isCorrect;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        buzz.stopVibration();
        buzz.disconnect();
    }
}