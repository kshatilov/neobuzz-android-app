package com.shatilov.neobuzz;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shatilov.neobuzz.utils.BuzzAwareActivity;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.utils.ColourPalette;
import com.shatilov.neobuzz.utils.EasyPredictor;
import com.shatilov.neobuzz.utils.HapticFeedbackActivity;
import com.shatilov.neobuzz.utils.MyoAwareActivity;
import com.shatilov.neobuzz.utils.MyoWrapper;
import com.shatilov.neobuzz.haptics.NaiveTranslator;
import com.shatilov.neobuzz.haptics.VibroTranslator;
import com.shatilov.neobuzz.widgets.BuzzWidget;
import com.shatilov.neobuzz.widgets.MyoWidget;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity implements
        HapticFeedbackActivity,
        MyoAwareActivity,
        BuzzAwareActivity {

    private static final String TAG = "Neo_Buzz_Main_Activity";
    private static final String ESP_URI = "http://192.168.137.86:5000/";
    private static final String TEST_STABLE_URI = "http://192.168.137.1:80/";
    private static final long ESP_POLLING_TIME = 200;

    /* MYO */
    private MyoWrapper myo;
    private MyoWidget myoWidget;

    /* BUZZ */
    private BuzzWrapper buzz;
    private BuzzWidget buzzWidget;

    /* CLF */
    private final Queue<float[]> q = new ArrayDeque<>(EasyPredictor.SAMPLES);
    private boolean isClfEnabled = false;
    private EasyPredictor clf;

    /* Misc */
    private final Hand hand = new Hand();
    private VibroTranslator translator;
    private HandPanel handPanel;
    private View buzzLabel;
    private View myoLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clf = new EasyPredictor(this);

        initBLEDevices();
        intiUI();
        initComm();

        translator = new NaiveTranslator(hand, buzz, myo);
    }

    private void initBLEDevices() {
        myo = new MyoWrapper(this);
        buzz = new BuzzWrapper(this);
    }

    private void intiUI() {
        LinearLayout controlContainer = findViewById(R.id.control_container);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.progress_anim);
        anim.setDuration(2000);

        // MYO UI
        Button myoCB = findViewById(R.id.myo_connect);
        myoLabel = findViewById(R.id.myo_label);
        myoCB.setOnClickListener((b) -> {
            if (!myo.isConnected()) {
                myoLabel.setBackground(getDrawable(R.drawable.icon_progress));
                myoLabel.startAnimation(anim);
                myo.connect();
            } else {
                myo.vibrate();
            }
        });
        LinearLayout myoWContainer =  findViewById(R.id.myo_widget);
        myoWidget = new MyoWidget(this);
        myoWContainer.addView(myoWidget);

        // BUZZ UI
        buzzLabel = findViewById(R.id.buzz_label);
        Button buzzCB = findViewById(R.id.buzz_connect);
        buzzCB.setOnClickListener((b) -> {
            if (!buzz.isConnected()) {
                buzzLabel.setBackground(getDrawable(R.drawable.icon_progress));
                buzzLabel.startAnimation(anim);
                buzz.connect();
            } else {
                buzz.sendVibration();
            }
        });
        LinearLayout buzzWContainer =  findViewById(R.id.buzz_widget);
        buzzWidget = new BuzzWidget(this);
        buzzWContainer.addView(buzzWidget);

        LinearLayout handContainer = findViewById(R.id.hand_container);
        handPanel = new HandPanel(this, hand);
        handContainer.addView(handPanel);

        // CLF UI
        Switch switchInput = findViewById(R.id.clf_switch);
        int[][] states = new int[][] {new int[] {-android.R.attr.state_checked},new int[] {android.R.attr.state_checked}};
        int[] thumbColors = new int[] {Color.GRAY,ColourPalette.neuralBlue};
        switchInput.setThumbTintList(new ColorStateList(states, thumbColors));
        switchInput.setChecked(isClfEnabled);
        switchInput.setOnCheckedChangeListener((e, isChecked) -> isClfEnabled = isChecked);
    }

    private void initComm() {

        // HTTP Server
        Thread serverThread = new Thread(() -> {
            NanoHTTPD server = new NanoHTTPD(5000) {
                @Override
                public Response serve(IHTTPSession session) {
                    return newFixedLengthResponse(hand.getGesture());
                }
            };
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            } catch (IOException e) {
                Log.d(TAG, "initComm: Failed to start the server");
            }
        });
        serverThread.start();

        // HTTP Client
        Thread clientThread = new Thread(() -> {
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, ESP_URI,
                    response -> {
                        if (null != response && !response.isEmpty()) {
                            Log.d(TAG, "response: " + response);
                        }
                    },
                    error -> {
                        Log.d(TAG, "Failed to connect " + error.getMessage());
                    });

            boolean keepOn = true;
            while (keepOn) {
                try {
                    queue.add(stringRequest);
                    Thread.sleep(ESP_POLLING_TIME);
                } catch (InterruptedException whatever) {
                    keepOn = false;
                }
            }
        });
        clientThread.start();
    }

    @Override
    public void onMyoConnect() {
        Toast toast = Toast.makeText(getApplicationContext(), "Myo is connected", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
        myoLabel.clearAnimation();
        myoLabel.setBackground(getDrawable(R.drawable.icon_connected));
    }

    @Override
    public void onBuzzConnect(int code) {
        if (code >= 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Buzz is connected", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            buzzLabel.clearAnimation();
            buzzLabel.setBackground(getDrawable(R.drawable.icon_connected));
        }
    }

    @Override
    public void onMyoData(float[] emgData) {
        myoWidget.update(emgData);
        if (!isClfEnabled) {
            return;
        }
        q.add(emgData);
        if (q.size() == EasyPredictor.SAMPLES) {
            for (int i = 0; i < EasyPredictor.SAMPLES / 2; ++i) {
                q.poll();
            }
            hand.setGesture(clf.predict(q));
            buzzWidget.update(translator.vibrate());
            handPanel.update();
        }
    }

    public void onHandUpdated() {
        buzzWidget.update(translator.vibrate());
    }
}