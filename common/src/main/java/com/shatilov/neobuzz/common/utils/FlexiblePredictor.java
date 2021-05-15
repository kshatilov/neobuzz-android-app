package com.shatilov.neobuzz.common.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Queue;

public class FlexiblePredictor {

    public static final int CHANNELS = 8;
    public static final int SAMPLES = 125;

    public String[] gestures = {
            "palm",
            "fist",
            "top",
            "mid",
            "bot"
    };

    private static final String TAG = "CLF";

    private Interpreter model;
    private Context context;

    public FlexiblePredictor(Context context) {
        this.context = context;
    }

    public void setModel(String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor = context.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        model = new Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength));
    }

    public String predict(Queue<float[]> q) {
        ByteBuffer result = ByteBuffer.allocateDirect(SAMPLES * CHANNELS * (Float.SIZE / Byte.SIZE));
        result.order(ByteOrder.LITTLE_ENDIAN);

        for (Object value : q) {
            byte[] converted = FloatArray2ByteArray((float[]) value);
            result.put(converted);
        }
        float[][] out = new float[1][5];
        model.run(result, out);

        float max = -1.F;
        int maxI = -1;

        for (int i = 0; i < 5; ++i) {
           if (max - out[0][i] < 0.00001) {
               maxI = i;
               max = out[0][i];
           }
        }

        return gestures[maxI];
    }


    static byte[] FloatArray2ByteArray(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate((Float.SIZE / Byte.SIZE) * values.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (float value : values) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }
}
