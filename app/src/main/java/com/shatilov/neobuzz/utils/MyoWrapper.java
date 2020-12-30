package com.shatilov.neobuzz.utils;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.ncorti.myonnaise.Myo;
import com.ncorti.myonnaise.MyoStatus;
import com.ncorti.myonnaise.Myonnaise;

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

    public void init() {
        Log.d(TAG, "getMYO: start");
        Myonnaise myonnaise = new Myonnaise(activity);
        String LEFT_MYO_ADDRESS = "CB:29:93:00:70:09";
        DisposableSingleObserver<Myo> ignored_1 = myonnaise.getMyo(LEFT_MYO_ADDRESS).subscribeWith(new DisposableSingleObserver<Myo>() {
            @Override
            public void onSuccess(@NonNull Myo _myo) {
                Log.d(TAG, "getMYO: success");
                myo = _myo;
                myo.connect(activity.getApplicationContext());

                Disposable ignored_2 = myo.statusObservable()
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
                Disposable ignored_3 = myo.dataFlowable()
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
}
