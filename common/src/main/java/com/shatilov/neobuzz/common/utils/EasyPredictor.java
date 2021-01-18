package com.shatilov.neobuzz.common.utils;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.ml.KirillNb125;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;

public class EasyPredictor {

    public static final int CHANNELS = 8;
    public static final int SAMPLES = 125;

    private static final String TAG = "CLF";

    private KirillNb125 model;

    public EasyPredictor(Context context) {
        try {
            model = KirillNb125.newInstance(context);
        } catch (IOException e) {
            Log.d(TAG, "CLFWrapper: failed to load the model" + e.getLocalizedMessage());
        }
    }

    public String predict(Queue<float[]> q) {
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, SAMPLES, CHANNELS, 1}, DataType.FLOAT32);

        ByteBuffer result = ByteBuffer.allocateDirect(SAMPLES * CHANNELS * (Float.SIZE / Byte.SIZE));
        result.order(ByteOrder.LITTLE_ENDIAN);

        for (Object value : q) {
            byte[] converted = FloatArray2ByteArray((float[]) value);
            result.put(converted);
        }

        long inferenceStartTime = SystemClock.elapsedRealtime();
        inputFeature0.loadBuffer(result);
        KirillNb125.Outputs outputs = model.process(inputFeature0);
        TensorBuffer out = outputs.getOutputFeature0AsTensorBuffer();
        long inferenceEndTime = SystemClock.elapsedRealtime();

        int[] res = out.getIntArray();
        int index = 0;
        for (int i = 0; i < res.length; i++) {
            if (1 == res[i]) {
                index = i;
                break;
            }
        }
        return Hand.simpleGestures[index];
    }


    private static byte[] FloatArray2ByteArray(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * values.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (float value : values) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }
}
