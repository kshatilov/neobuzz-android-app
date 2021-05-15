package hk.ust.shatilov.myokey3;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.shatilov.neobuzz.common.utils.EasyPredictor;
import com.shatilov.neobuzz.common.utils.FlexiblePredictor;
import com.shatilov.neobuzz.common.utils.MyoAwareActivity;
import com.shatilov.neobuzz.common.utils.MyoWrapper;
import com.shatilov.neobuzz.common.widgets.MyoWidget;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import fi.iki.elonen.NanoHTTPD;

public class MKActivity
        extends AppCompatActivity implements MyoAwareActivity {

    private static final String TAG = "Neo_Buzz_DC_Activity";

    /* MYO */
    private MyoWidget myoWidget;
    private final MyoWrapper myo = new MyoWrapper(this);

    /* CLF */
    private final Queue<float[]> q = new ArrayDeque<>(EasyPredictor.SAMPLES);
    private FlexiblePredictor clf;
    private final List<String> modelsList = new ArrayList<>();
    private String gesture = "palm";
    private long gesture_timestamp;
    private ClfWidget clfWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mk_activity);
        clf = new FlexiblePredictor(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 0);

        initML();
        intiUI();
        initBLE();
        initWeb();
    }

    private void initWeb() {
        Thread gestureThread = new Thread(() -> {
            NanoHTTPD server = new NanoHTTPD(5050) {
                @Override
                public Response serve(IHTTPSession session) {
                    return newFixedLengthResponse(
                            "callback({" +
                                    "\"id\": \"" + gesture_timestamp + "\", " +
                                    "\"gesture\":\"" + gesture + "\"" +
                                    "})");
                }
            };
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            } catch (IOException e) {
                Log.d(TAG, "initComm: Failed to start gesture server");
            }
        });
        gestureThread.start();

        String htmlContent = "";
        try {
            Resources res = getResources();
            InputStream in_s = res.openRawResource(R.raw.main);
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            htmlContent = new String(b);
        } catch (Exception e) {
            Log.e(TAG, "initWeb:  failed to load html");
        }
        String finalHtmlContent = htmlContent;

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.progress_anim);
        anim.setDuration(2000);
        final boolean[] isAnimated = {false};

        Thread htmlThread = new Thread(() -> {
            NanoHTTPD server = new NanoHTTPD(5000) {
                @Override
                public Response serve(IHTTPSession session) {
                    if (!isAnimated[0]) {
                        findViewById(R.id.web_icon).startAnimation(anim);
                        isAnimated[0] = true;
                        TextView incIp = ((TextView)findViewById(R.id.inc_ip_label));
                        incIp.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        incIp.setText(session.getRemoteIpAddress());
                    }
                    return newFixedLengthResponse(finalHtmlContent);
                }
            };
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            } catch (IOException e) {
                Log.d(TAG, "initComm: Failed to start html server");
            }
        });
        htmlThread.start();


    }

    private void initML() {
        try {
            AssetManager manager = getAssets();
            String[] assetsNames = manager.list("");
            for (String assetName :
                    assetsNames) {
                if (assetName.endsWith(".tflite") && assetName.contains("2021")) {
                    modelsList.add(assetName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initBLE() {
        myo.connect();
    }

    private void intiUI() {
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#202020"));

        // MYO UI
        LinearLayout myoWContainer = findViewById(R.id.myo_widget);
        myoWidget = new MyoWidget(this);
        myoWContainer.addView(myoWidget);

        // CLF SELECTOR
        Spinner clfSelector = findViewById(R.id.clf_selector);
        Log.d(TAG, "intiUI: clf selector");
        Log.d(TAG, "intiUI: " + modelsList.toString());
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, modelsList);
        clfSelector.setSelection(1);

        clfSelector.setAdapter(adapter);
        clfSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                ((TextView) adapterView.getChildAt(0)).setTextSize(20);
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);
                try {
                    clf.setModel(modelsList.get(position));
                    makeToast("Loaded " + modelsList.get(position));
                } catch (IOException e) {
                    makeToast(e.getLocalizedMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // CLF UI
        LinearLayout clfWContainer = findViewById(R.id.clf_widget);
        clfWidget = new ClfWidget(this);
        clfWContainer.addView(clfWidget);

        // Web UI
        WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        TextView ipLabel = findViewById(R.id.ip_label);
        ipLabel.setText(ip + ":5000");

    }

    private void makeToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public void onMyoConnect() {
        makeToast("Myo is connected");
    }

    public void onMyoData(float[] emgData) {
        myoWidget.update(emgData);

        // scale input
        for (int i = 0; i < 8; i++) {
            emgData[i] /= 128.F;
        }

        q.add(emgData);
        if (q.size() == EasyPredictor.SAMPLES) {
            String label = clf.predict(q);
            clfWidget.update(label);
            q.clear();
            gesture = label;
            gesture_timestamp = System.currentTimeMillis();
        }
    }
}