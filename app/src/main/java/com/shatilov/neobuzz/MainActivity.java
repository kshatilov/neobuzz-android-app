package com.shatilov.neobuzz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;

import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.shatilov.neobuzz.utils.BuzzAwareActivity;
import com.shatilov.neobuzz.utils.BuzzWrapper;
import com.shatilov.neobuzz.utils.EasyPredictor;
import com.shatilov.neobuzz.utils.MyoAwareActivity;
import com.shatilov.neobuzz.utils.MyoWrapper;

import java.util.ArrayDeque;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements MyoAwareActivity, BuzzAwareActivity {

    private static final String TAG = "Neo_Buzz_Main_Activity";

    /* MYO */
    private MyoWrapper myo;
    private boolean isMyoConnected = false;

    /* BUZZ */
    private BuzzWrapper buzz;
    private boolean isBuzzConnected = false;

    /* CLF */
    Queue<float[]> q = new ArrayDeque<>(EasyPredictor.SAMPLES);
    EasyPredictor clf;
    String gesture = "00000";

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
        LinearLayout container = findViewById(R.id.hand_container);

        Button myoCB = new Button(this);
        myoCB.setText("Myo");
        myoCB.setOnClickListener((b) -> {
            if (!isMyoConnected) {
                myo.connect();
            } else {
                myo.vibrate();
            }
        });
        container.addView(myoCB);

        Button buzzCB = new Button(this);
        buzzCB.setText("Buzz");
        buzzCB.setOnClickListener((b) -> {
            if (!isBuzzConnected) {
                buzz.connect();
            } else {
                buzz.sendVibration();
            }
        });
        container.addView(buzzCB);

    }

    private void initComm() {
        // HTTP Server
        Handler serverThread = new Handler();
        serverThread.post(() -> {
            AsyncHttpServer server = new AsyncHttpServer();
            server.get("/", (request, response) -> response.send(gesture));
            server.listen(5000);
        });
    }

    @Override
    public void onMyoConnect() {
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
        }
    }

    @Override
    public void onBuzzConnect(int code) {
        if (code >= 0) {
            isBuzzConnected = true;
        }
    }
}