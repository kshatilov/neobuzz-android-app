package com.shatilov.us.ncp.neobuzz.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.neosensory.neosensoryblessed.NeosensoryBlessed;

import java.util.Arrays;

public class BuzzWrapper {
    public static final String BUZZ_ADDRESS = "FD:01:54:6E:6B:3C";
    private static final String TAG = "BuzzWrapper: ";
    private final Activity activity;
    NeosensoryBlessed buzz = null;
    private Thread vibratingPatternThread;
    int[] motorPattern;
    public int MAX_VIBRATION = NeosensoryBlessed.MAX_VIBRATION_AMP;

    public BuzzWrapper(Activity activity) {
        this.activity = activity;
    }

    class VibratingPattern implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.d(TAG, "sendPersistentVibration: " + Arrays.toString(motorPattern));
                    Thread.sleep(150);
                    buzz.vibrateMotors(motorPattern);
                } catch (InterruptedException e) {
                    buzz.stopMotors();
                    Log.i(TAG, "Interrupted thread");
                }
            }

        }
    }

    public void stopVibration() {
        if (null == buzz) {
            return;
        }
        buzz.stopMotors();
    }

    public void sendVibration(int[] motorPattern) {
        if (null == buzz) {
            return;
        }
        buzz.vibrateMotors(motorPattern);
    }

    private final BroadcastReceiver BlessedReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.hasExtra("com.neosensory.neosensoryblessed.CliReadiness")) {
                        if (intent.getBooleanExtra("com.neosensory.neosensoryblessed.CliReadiness", false)) {
                            buzz.sendDeveloperAPIAuth();
                            buzz.acceptApiTerms();
                            buzz.pauseDeviceAlgorithm();
                            Log.d(TAG, String.format("state message: %s", buzz.getNeoCliResponse()));
                            if (activity instanceof BuzzAwareActivity) {
                                ((BuzzAwareActivity) activity).handleConnect(1);
                            }
                        }
                    }

                    if (intent.hasExtra("com.neosensory.neosensoryblessed.CliMessage")) {
                        String notification_value =
                                intent.getStringExtra("com.neosensory.neosensoryblessed.CliMessage");
                    }

                    if (intent.hasExtra("com.neosensory.neosensoryblessed.ConnectedState")) {
                        if (intent.getBooleanExtra("com.neosensory.neosensoryblessed.ConnectedState", false)) {
                            Log.i(TAG, "Connected to Buzz");
                        } else {
                            Log.i(TAG, "Disconnected from Buzz");
                        }
                    }
                }
            };

    private void initBluetoothHandler() {
        buzz = NeosensoryBlessed.getInstance(activity, new String[]{"Buzz"}, false);
        activity.registerReceiver(BlessedReceiver, new IntentFilter("BlessedBroadcast"));
        buzz.attemptNeoReconnect();
    }

    public void init() {
        NeosensoryBlessed.requestBluetoothOn(activity);
        initBluetoothHandler();
    }
}
