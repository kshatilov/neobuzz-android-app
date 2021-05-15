package com.shatilov.neobuzz.common.utils;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.shatilov.neobuzz.common.utils.EasyPredictor.FloatArray2ByteArray;

public class BuzzEncoder {

    private static final String TAG = "CLF";

//    private Encoder model;

    public BuzzEncoder(Context context) {
//        try {
//            model = Encoder.newInstance(context);
//        } catch (IOException e) {
//            Log.d(TAG, "CLFWrapper: failed to load the model" + e.getLocalizedMessage());
//        }
    }

    public int[] predict(float[] input) {
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 140}, DataType.FLOAT32);
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(140 * (Float.SIZE / Byte.SIZE));
        inputBuffer.order(ByteOrder.LITTLE_ENDIAN);

        inputBuffer.put(FloatArray2ByteArray(input));

        long inferenceStartTime = SystemClock.elapsedRealtime();
        inputFeature0.loadBuffer(inputBuffer);
//        Encoder.Outputs outputs = model.process(inputFeature0);
//        TensorBuffer out = outputs.getOutputFeature0AsTensorBuffer();
        long inferenceEndTime = SystemClock.elapsedRealtime();

//        float[] fRes = out.getFloatArray();
        int[] res = new int[4];
//        for (int i = 0; i < 4; i++) {
//            res[i] = (int) (fRes[i] * 255);
//        }

        Log.d(TAG, "predict: " + Arrays.toString(res));
        return res;
    }


}
