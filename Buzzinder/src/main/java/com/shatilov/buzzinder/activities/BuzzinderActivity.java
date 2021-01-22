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
import com.shatilov.neobuzz.common.haptics.HapticProfile;
import com.shatilov.neobuzz.common.haptics.HapticTranslator;
import com.shatilov.neobuzz.common.haptics.NaiveTranslator;
import com.shatilov.neobuzz.common.haptics.PatternTranslator;
import com.shatilov.neobuzz.common.utils.BuzzAwareActivity;
import com.shatilov.neobuzz.common.utils.BuzzWrapper;
import com.shatilov.neobuzz.common.utils.ColourPalette;
import com.shatilov.neobuzz.common.utils.Utils;
import com.shatilov.neobuzz.common.widgets.BuzzWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BuzzinderActivity extends AppCompatActivity implements BuzzAwareActivity, DeckActivity {

    private static final String TAG = "Buzzinder Activity";
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
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        buzzWidget = new BuzzWidget(getApplicationContext());
        buzz = new BuzzWrapper(this, buzzWidget);
        buzz.connect();
        initDeck();

        LinearLayout deckContainer = findViewById(R.id.deck_container);
        deckWidget = new DeckWidget(this, buzzWidget);
        deckContainer.addView(deckWidget);
        deckContainer.setOnClickListener((e) -> dismissPopup());

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

        initPopup();
    }

    private void initDeck() {
        String selection = getIntent().getExtras().getString(StartActivity.SELECTION);
        if (selection.equals("Naive")) {
            initNaiveDeck();
        } else {
            Map<String, HapticTranslator> config = Utils.initHapticConfig(new Hand(), buzz);
            if (config.containsKey(selection)) {
                initPatternDeck(config.get(selection));
            } else {
                Log.d(TAG, "initDeck: Wrong intent, selecting naive buzzes");
                initNaiveDeck();
            }
        }
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
        Collections.shuffle(deck);
        vibrator = new NaiveTranslator(new Hand(), buzz);
    }

    private void initPatternDeck(HapticTranslator vibrator) {
        this.vibrator = vibrator;
        HapticProfile profile = ((PatternTranslator) vibrator).getHapticProfile();
        List<HapticProfile.GesturePattern> patterns = profile.getPatterns();
        deck = new ArrayList<>();
        for (HapticProfile.GesturePattern gesturePattern : patterns) {
            deck.add(new Hand(gesturePattern.getGesture()));
        }
        Collections.shuffle(deck);
    }

    private void initPopup() {
        popup = new PopupWindow(this);
        LinearLayout layout = new LinearLayout(this);
        result = new TextView(this);
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
        layout.setOnClickListener((e) -> dismissPopup());
        result.setOnClickListener((e) -> dismissPopup());
    }

    private void dismissPopup() {
        popup.dismiss();
        deckWidget.setAlpha(1.F);
    }

    @Override
    public void onBuzzConnect(int code) {
        if (code >= 0) {
            vibrator.setBuzz(buzz);
            Toast toast = Toast.makeText(getApplicationContext(), "Buzz is connected", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            vibrator.vibrate();
        }
    }

    public void showResult(boolean isCorrect) {
        int id = -1;
        if (isCorrect) {
            id = impostor ? R.string.yay : R.string.it_s_a_match;
        } else {
            id = R.string.nope;
        }
        result.setText(getResources().getText(id));
        deckWidget.setAlpha(.4F);
        popup.showAtLocation(findViewById(R.id.main_container), Gravity.CENTER, 0, 0);
        popup.update(0, 300, 1000, 1000);
    }

    @Override
    public boolean swipe(boolean right) {
        buzz.stopVibration();
        dismissPopup();
        boolean left = !right; // aha
        boolean isCorrect = !impostor && right || impostor && left;
        if (isCorrect) {
            scoreWidget.setCurrentScore(++score);
        }
        showResult(isCorrect);
        if (deckPointer < deck.size() - 1) {
            Hand hand = deck.get(++deckPointer);
            Random rnd = new Random();
            int dice = ThreadLocalRandom.current().nextInt(0, 1000);
            deckWidget.setHand(hand);
            if (dice > 500) {
                // get vibration for a random hand from the deck
                Log.d(TAG, "swipe: Careful, it's an impostor!");
                impostor = true;
                Hand impostorHand = null;
                int index = -1;
                do {
                    index = ThreadLocalRandom.current().nextInt(1, deck.size() - 1);
                } while (index == deckPointer);
                // but not the one that is picked for the handWidget
                impostorHand = deck.get(index);
                vibrator.setHand(impostorHand);
            } else {
                impostor = false;
                vibrator.setHand(hand);
            }
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