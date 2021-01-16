package com.shatilov.neobuzz;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shatilov.neobuzz.haptics.PatternTranslator;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity implements
        HapticFeedbackActivity,
        MyoAwareActivity,
        BuzzAwareActivity {

    private static final String TAG = "Neo_Buzz_Main_Activity";
    private static final String ESP_URI = "http://192.168.137.104:5000/";
    private static final String TEST_STABLE_URI = "http://192.168.137.1:80/";
    private static final long ESP_POLLING_TIME = 200;

    /* MYO */
    private MyoWrapper myo;
    private MyoWidget myoWidget;

    /* BUZZ */
    private BuzzWrapper buzz;
    private BuzzWidget buzzWidget;
    private VibroTranslator translator;
    private NaiveTranslator naiveTranslator;
    private PatternTranslator patternTranslator;


    /* CLF */
    private final Queue<float[]> q = new ArrayDeque<>(EasyPredictor.SAMPLES);
    private boolean isClfEnabled = false;
    private EasyPredictor clf;

    /* Misc */
    private Thread clientThread;
    private boolean clientThreadIsRunning = false;
    private final Hand hand = new Hand();
    private HandPanel handPanel;
    private View buzzLabel;
    private View myoLabel;
    private String espUri = ESP_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clf = new EasyPredictor(this);

        intiUI();
        initBLEDevices();
        initComm();
        intiHaptic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myo.disconnect();
        buzz.disconnect();
    }

    private void initBLEDevices() {
        myo = new MyoWrapper(this);
        buzz = new BuzzWrapper(this, buzzWidget);
    }

    private void intiUI() {
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
        LinearLayout myoWContainer = findViewById(R.id.myo_widget);
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
        LinearLayout buzzWContainer = findViewById(R.id.buzz_widget);
        buzzWidget = new BuzzWidget(this);
        buzzWContainer.addView(buzzWidget);

        LinearLayout handContainer = findViewById(R.id.hand_container);
        handPanel = new HandPanel(this, hand);
        handContainer.addView(handPanel);

        // CLF UI
        Switch switchInput = findViewById(R.id.clf_switch);
        int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};
        int[] thumbColors = new int[]{Color.GRAY, ColourPalette.neuralBlue};
        switchInput.setThumbTintList(new ColorStateList(states, thumbColors));
        switchInput.setChecked(isClfEnabled);
        switchInput.setOnCheckedChangeListener((e, isChecked) -> isClfEnabled = isChecked);
    }

    private void intiHaptic() {
        naiveTranslator = new NaiveTranslator(getApplicationContext(), hand, buzz);
        naiveTranslator.setMyo(myo);
        translator = naiveTranslator;
        patternTranslator = new PatternTranslator(getApplicationContext(), hand, buzz);
        Spinner typeSpinner = findViewById(R.id.type_spinner);
        Map<String, VibroTranslator> hapticOptions = new TreeMap<>();
        hapticOptions.put("Naive", naiveTranslator);
        hapticOptions.put("type_1", patternTranslator);
        List<String> optionList = new ArrayList<>(hapticOptions.keySet());
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, optionList);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                translator = hapticOptions.get(optionList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        typeSpinner.setAdapter(adapter);
    }

    private void initComm() {
        // HTTP Client
        clientThread = new Thread(() -> {
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, espUri,
                    response -> {
                        if (null != response && !response.isEmpty()) {
                            Log.d(TAG, "response: " + response);
                            // TODO process response
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

        // HTTP Server
        Thread serverThread = new Thread(() -> {
            NanoHTTPD server = new NanoHTTPD(5000) {
                @Override
                public Response serve(IHTTPSession session) {
                    if (!clientThreadIsRunning) {
                        espUri = "http://" + session.getRemoteIpAddress() + ":5000";
                        clientThread.start();
                        clientThreadIsRunning = true;
                    }
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
        // scale input
        for (int i = 0; i < 8; i++) {
            emgData[i] /= MyoWrapper.EMG_MAX_VALUE;
        }
        q.add(emgData);
        if (q.size() == EasyPredictor.SAMPLES) {
            hand.setGesture(clf.predict(q));
            q.clear(); // no window overlap
            translator.vibrate();
            handPanel.update();
        }
    }

    public void onHandUpdated() {
        if (null == translator) {
            translator = naiveTranslator;
        }
        translator.vibrate();
    }
}