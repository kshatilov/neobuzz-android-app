package com.shatilov.neobuzz;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import com.shatilov.neobuzz.haptics.HapticProfile;
import com.shatilov.neobuzz.utils.BuzzAwareActivity;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.utils.ColourPalette;
import com.shatilov.neobuzz.utils.EasyPredictor;
import com.shatilov.neobuzz.utils.HapticFeedbackActivity;
import com.shatilov.neobuzz.utils.MyoAwareActivity;
import com.shatilov.neobuzz.utils.MyoWrapper;
import com.shatilov.neobuzz.haptics.NaiveTranslator;
import com.shatilov.neobuzz.haptics.HapticTranslator;
import com.shatilov.neobuzz.widgets.BuzzWidget;
import com.shatilov.neobuzz.widgets.MyoWidget;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
    private static final long ESP_POLLING_TIME = 200;

    /* MYO */
    private MyoWrapper myo;
    private MyoWidget myoWidget;

    /* BUZZ */
    private BuzzWrapper buzz;
    private BuzzWidget buzzWidget;
    private HapticTranslator translator = null;
    private NaiveTranslator naiveTranslator;

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
    private String espUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clf = new EasyPredictor(this);

        intiUI();
        initBLEDevices();
        initComm();
        initHaptic();

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

    private void initHaptic() {
        Map<String, HapticTranslator> hapticOptions = new TreeMap<>();

        naiveTranslator = new NaiveTranslator(hand, buzz);
        naiveTranslator.setMyo(myo);
        translator = naiveTranslator;
        hapticOptions.put("Naive", naiveTranslator);

        String path = Environment.getExternalStorageDirectory().toString() + "/BuzzProfiles";
        File directory = new File(path);
        File[] files = directory.listFiles();
        String fName = null;
        // look for haptic profiles
        for (File file : files) {
            try {
                fName = file.getName();
                String cfgName = fName.split("\\.")[0];
                FileInputStream fStream = new FileInputStream(new File(path + "/" + fName));
                String json = IOUtils.toString(fStream, StandardCharsets.UTF_8);
                HapticProfile pattern = new HapticProfile(json);
                PatternTranslator translator = new PatternTranslator(hand, buzz);
                translator.setHapticProfile(pattern);
                hapticOptions.put(cfgName, translator);
            } catch (IOException e) {
                Log.e(TAG, "initHaptic: Failed to load haptic profile from file" + fName, e);
            }
        }
        Spinner typeSpinner = findViewById(R.id.type_spinner);
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