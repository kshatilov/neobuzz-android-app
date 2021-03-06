package com.shatilov.neobuzz.common.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.neosensory.neosensoryblessed.NeoBuzzPsychophysics;
import com.neosensory.neosensoryblessed.NeosensoryBlessed;
import com.shatilov.neobuzz.common.widgets.BuzzWidget;

import java.util.ArrayList;
import java.util.List;

public class BuzzWrapper {
    public static final String BUZZ_ADDRESS = "FD:01:54:6E:6B:3C";
    public static final int FPS = 8; // control frames per second, should be 64
    private static final String TAG = "BuzzWrapper: ";
    private static final long SWIPE_TIME = 4000;

    private final Activity activity;
    private final BuzzWidget widget;
    NeosensoryBlessed buzz = null;
    private Thread vibratingThread;
    private Thread notifyThread;
    boolean vibrating = false;
    public int MAX_VIBRATION = NeosensoryBlessed.MAX_VIBRATION_AMP;
    private boolean direction;
    private boolean isConnected = false;

    private List<int[]> patterns;
    private long interval = 500;

    public BuzzWrapper(Activity activity, BuzzWidget widget) {
        this.activity = activity;
        this.widget = widget;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public boolean isConnected() {
        return isConnected;
    }

    class NotifyThread implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted() && vibrating) {
                List<int[]> patternCopy = new ArrayList<>(patterns);
                patternCopy.forEach(pattern -> {
                    // notify activity
                    try {
                        Thread.sleep(SWIPE_TIME / FPS);
                    } catch (InterruptedException e) {
                        buzz.stopMotors();
                    }
                });
            }
        }
    }

    void generatePattern() {
        patterns = new ArrayList<>();

        float intensity = 1.F;

        for (int i = 0; i < FPS; i++) {
            float location = (i + 1) * (1.F / FPS);
            if (!direction) location = 1 - location;
            patterns.add(NeoBuzzPsychophysics.GetIllusionActivations(intensity, location));
        }
    }

    class VibratingPattern implements Runnable {
        public void run() {
            while (vibrating && !Thread.currentThread().isInterrupted()) {
                patterns.forEach(pattern -> {

                    buzz.vibrateMotors(pattern);
                });
                try {
                    Thread.sleep(SWIPE_TIME);
                } catch (InterruptedException e) {
                    buzz.clearMotorQueue();
                    buzz.stopMotors();
                }
            }
        }
    }

    public void stopVibration() {
        if (null == buzz) {
            return;
        }

        vibrating = false;
        if (vibratingThread != null) {
            vibratingThread.interrupt();
        }

        if (notifyThread != null) {
            notifyThread.interrupt();
        }

        if (null != widget) {
            widget.update(new int[]{0, 0, 0, 0});
        }

        buzz.clearMotorQueue();
        buzz.stopMotors();
        buzz.attemptNeoReconnect();
    }

    public void sendVibration(List<int[]> patterns) {
        if (null == buzz || patterns == null) {
            return;
        }
        vibrating = true;
        vibratingThread = new Thread(() -> {
            int i = 0;
            while (vibrating && !Thread.currentThread().isInterrupted()) {
                int[] pattern = patterns.get(i % patterns.size());
                buzz.vibrateMotors(pattern);
                if (null != widget) widget.update(pattern);
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    vibrating = false;
                }
                i++;
            }
        });
        vibratingThread.start();
    }

    public void sendVibration(int[] motorPattern) {
        if (null == buzz) {
            return;
        }
        if (null != widget) {
            widget.update(motorPattern);
        }
        buzz.vibrateMotors(motorPattern);
    }

    public void sendVibration() {
        if (null == buzz) {
            return;
        }
        buzz.vibrateMotors(new int[]{255, 255, 255, 255});
    }

    /* if direction: left-to-right */
    public void sendSwipe(boolean direction) {
        if (null == buzz) {
            return;
        }
        vibrating = true;
        this.direction = direction;

        generatePattern();

        vibratingThread = new Thread(new VibratingPattern());
        vibratingThread.start();

        notifyThread = new Thread(new NotifyThread());
        notifyThread.start();
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
                            isConnected = true;
                            if (activity instanceof BuzzAwareActivity) {
                                ((BuzzAwareActivity) activity).onBuzzConnect(1);
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

    public void connect() {
        NeosensoryBlessed.requestBluetoothOn(activity);
        initBluetoothHandler();
    }

    public void disconnect() {
        if (!isConnected) {
            return;
        }
        buzz.disconnectNeoDevice();
        isConnected = false;
    }
}
