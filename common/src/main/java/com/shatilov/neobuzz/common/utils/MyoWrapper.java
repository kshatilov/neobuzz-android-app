package com.shatilov.neobuzz.common.utils;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.ncorti.myonnaise.Myo;
import com.ncorti.myonnaise.MyoStatus;
import com.ncorti.myonnaise.Myonnaise;
import com.shatilov.neobuzz.common.haptics.VibrationIntensity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MyoWrapper {

    private static final int MYO_POLLING_FREQUENCY = 200;
    public static final float EMG_MAX_VALUE = 128.F;
    private static final short VIBRO_DURATION = 100;
    private static final String TAG = "MyoWrapper: ";

    private final Activity activity;
    private Myo myo = null;
    private boolean isConnected = false;
    private boolean isVibrating = false;
    private VibrationIntensity intensity = VibrationIntensity.HIGH;
    private Thread vibroThread;

    public MyoWrapper(Activity activity) {
        this.activity = activity;
    }

    public void connect() {
        Log.d(TAG, "getMYO: start");
        Myonnaise myonnaise = new Myonnaise(activity);
        String LEFT_MYO_ADDRESS = "CB:29:93:00:70:09";
        DisposableSingleObserver<Myo> ignored_once = myonnaise.getMyo(LEFT_MYO_ADDRESS).subscribeWith(new DisposableSingleObserver<Myo>() {
            @Override
            public void onSuccess(@NonNull Myo _myo) {
                Log.d(TAG, "getMYO: success");
                myo = _myo;
                myo.connect(activity.getApplicationContext());

                Disposable ignored_twice = myo.statusObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            if (it == MyoStatus.READY) {
                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    myo.sendCommand(Utils.getStreamCmd());
                                    myo.setFrequency(MYO_POLLING_FREQUENCY);
                                }, 1000);

                                isConnected = true;

                                if (activity instanceof MyoAwareActivity) {
                                    ((MyoAwareActivity) activity).onMyoConnect();
                                }
                            }
                        });
                Disposable ignored_three_times = myo.dataFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onBackpressureDrop()
                        .subscribe(activity instanceof MyoAwareActivity ? ((MyoAwareActivity) activity)::onMyoData : (value) -> {
                        });
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.d(TAG, "onError: Failed to connect to myo" + e.getLocalizedMessage());
            }
        });
    }

    /**
     * Myo complex vibration pattern with fixed intensity
     * see https://github.com/thalmiclabs/myo-bluetooth/blob/master/myohw.h
     */
    public void vibrate() {
        byte[] cmd = new byte[20];
        byte pos = 0;
        byte command_vibrate = 0x07;
        cmd[pos++] = command_vibrate;
        cmd[pos++] = 18;
        short steps = 6;
        byte strength = (byte) intensity.getValue();
        short duration = (short) (VIBRO_DURATION / steps);
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(duration);
        for (int i = 0; i < steps; i++) {
            cmd[pos++] = bb.get(0);
            cmd[pos++] = bb.get(1);
            cmd[pos++] = strength;
        }
        myo.sendCommand(cmd);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void stopVibration() {
        isVibrating = false;
        if (null != vibroThread) vibroThread.interrupt();
    }

    public void sendPersistentVibration(VibrationIntensity intensity) {
        this.intensity = intensity;
        if (intensity == VibrationIntensity.NONE) {
            if (isVibrating) {
                isVibrating = false;
                if (null != vibroThread) vibroThread.interrupt();
            }
        }
        vibroThread = new Thread(() -> {
            while (isVibrating && !Thread.currentThread().isInterrupted()) {
                try {
                    if (this.intensity.getValue() == 0) {
                        return;
                    }

                    vibrate();

                    Thread.sleep(VIBRO_DURATION);

                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        isVibrating = true;
        vibroThread.start();
    }

    public void disconnect() {
        if (!isConnected) {
            return;
        }
        myo.disconnect();
        isConnected = false;
    }
}
