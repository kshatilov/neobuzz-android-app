package com.shatilov.neobuzz.utils;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.ncorti.myonnaise.CommandList;
import com.ncorti.myonnaise.Myo;
import com.ncorti.myonnaise.MyoStatus;
import com.ncorti.myonnaise.Myonnaise;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MyoWrapper {

    private static int MYO_POLLING_FREQUENCY = 200;
    private static final String TAG = "MyoWrapper: ";

    private final Activity activity;
    private Myo myo = null;

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
                                }, 2000);

                                if (activity instanceof MyoAwareActivity) {
                                    ((MyoAwareActivity) activity).onMyoConnect();
                                }
                            }
                        });
                Disposable ignored_three_times = myo.dataFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onBackpressureDrop()
                        .subscribe(activity instanceof MyoAwareActivity ? ((MyoAwareActivity) activity)::onMyoData : (value) -> {});
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.d(TAG, "onError: Failed to connect to myo" + e.getLocalizedMessage());
            }
        });
    }

    /**
     *  swipe up: increasing vibration intensity over 1200ms with a step of 200ms
     *  see https://github.com/thalmiclabs/myo-bluetooth/blob/master/myohw.h
     * */
    // TODO static initialize + configurable vibration
    public void vibrate() {
        byte[] cmd = new byte[20];
        byte pos = 0;
        byte command_vibrate = 0x07;
        cmd[pos++] = command_vibrate;
        cmd[pos++] = 18;
        int steps = 6;
        byte strength = (byte)50; // 0-255
        byte duration = (byte)200;
        for (int i = 0; i < steps; i++) {
            ByteBuffer bb = ByteBuffer.allocate(2);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            cmd[pos++] = duration;
            cmd[pos++] = 0x0;
            cmd[pos++] = strength;
            strength += 30;
        }
        myo.sendCommand(cmd);
    }

    public void disconnect() {
        myo.disconnect();
    }
}
