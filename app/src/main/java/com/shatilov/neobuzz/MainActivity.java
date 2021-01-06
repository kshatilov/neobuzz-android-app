package com.shatilov.neobuzz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shatilov.neobuzz.utils.BuzzAwareActivity;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.utils.EasyPredictor;
import com.shatilov.neobuzz.utils.MyoAwareActivity;
import com.shatilov.neobuzz.utils.MyoWrapper;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity implements MyoAwareActivity, BuzzAwareActivity {

    private static final String TAG = "Neo_Buzz_Main_Activity";
    private static final String ESP_URI = "http://192.168.137.122:5000/";
    private static final String TEST_STABLE_URI = "http://192.168.137.1:80/";

    /* MYO */
    private MyoWrapper myo;
    private boolean isMyoConnected = false;

    /* BUZZ */
    private BuzzWrapper buzz;
    private boolean isBuzzConnected = false;

    /* CLF */
    Queue<float[]> q = new ArrayDeque<>(EasyPredictor.SAMPLES);
    EasyPredictor clf;
    boolean isClfActive = true;
    String gesture = "00000";

    // TODO remove
    Button tmpLabel;

    private HandPanel handPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clf = new EasyPredictor(this);

        initBLEDevices();
        intiUI();
        initComm();
    }

    private void initBLEDevices() {
        myo = new MyoWrapper(this);
        buzz = new BuzzWrapper(this);
    }

    private void intiUI() {
        LinearLayout controlContainer = findViewById(R.id.control_container);

        Button myoCB = new Button(this);
        myoCB.setText("Myo");
        myoCB.setOnClickListener((b) -> {
            if (!isMyoConnected) {
                myo.connect();
            } else {
                myo.vibrate();
            }
        });
        controlContainer.addView(myoCB);

        Button buzzCB = new Button(this);
        buzzCB.setText("Buzz");
        buzzCB.setOnClickListener((b) -> {
            if (!isBuzzConnected) {
                buzz.connect();
            } else {
                buzz.sendVibration();
            }
        });
        controlContainer.addView(buzzCB);

        tmpLabel = new Button(this);
        controlContainer.addView(tmpLabel);


        LinearLayout handContainer = findViewById(R.id.hand_container);
        handPanel = new HandPanel(this);
        handContainer.addView(handPanel);

    }

    private void initComm() {

        // HTTP Server
        Thread serverThread = new Thread(() -> {
            NanoHTTPD server = new NanoHTTPD(5000) {
                @Override
                public Response serve(IHTTPSession session) {
                    return newFixedLengthResponse(gesture);
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
                            tmpLabel.setText(response);
                        }
                    },
                    error -> {
                        Log.d(TAG, "Failed to connect " + error.getMessage());
                    });

            boolean keepOn = true;
            while (keepOn) {
                try {
                    queue.add(stringRequest);
                    Thread.sleep(200);
                } catch (InterruptedException whatever) {
                    keepOn = false;
                }
            }
        });
        clientThread.start();
    }

    @Override
    public void onMyoConnect() {
        Toast.makeText(getApplicationContext(), "Myo is connected", Toast.LENGTH_SHORT).show();
        isMyoConnected = true;
    }

    @Override
    public void onMyoData(float[] emgData) {
        q.add(emgData);
        if (q.size() == EasyPredictor.SAMPLES) {
            gesture = clf.predict(q);


            for (int i = 0; i < EasyPredictor.SAMPLES / 2; ++i) {
                q.poll();
            }

            float[] pos = new float[5];
            for (int i = 0; i < 5; i++) {
                pos[i] = Float.parseFloat(String.valueOf(gesture.charAt(i)));
            }
            handPanel.setPos(pos);


        }
    }

    @Override
    public void onBuzzConnect(int code) {
        if (code >= 0) {
            Toast.makeText(getApplicationContext(), "Buzz is connected", Toast.LENGTH_SHORT).show();
            isBuzzConnected = true;
        }
    }
}