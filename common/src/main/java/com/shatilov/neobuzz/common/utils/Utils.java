package com.shatilov.neobuzz.common.utils;

import android.os.Environment;
import android.util.Log;

import com.shatilov.neobuzz.common.Hand;
import com.shatilov.neobuzz.common.haptics.HapticProfile;
import com.shatilov.neobuzz.common.haptics.HapticTranslator;
import com.shatilov.neobuzz.common.haptics.PatternTranslator;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
    public static byte[] getStreamCmd() {
        byte command_data = (byte) 0x01;
        byte payload_data = (byte) 3;
        byte emg_mode = (byte) 0x02;
        byte imu_mode = (byte) 0x00;
        byte class_mode = (byte) 0x00;

        return new byte[]{command_data, payload_data, emg_mode, imu_mode, class_mode};
    }

    public static Map<String, HapticTranslator> initHapticConfig(Hand hand, BuzzWrapper buzz) {
        Map<String, HapticTranslator> hapticOptions = new TreeMap<>();
        String path = Environment.getExternalStorageDirectory().toString() + "/BuzzProfiles";
        File directory = new File(path);
        File[] files = directory.listFiles();
        String fName = null;
        if (files.length == 0) {
            Log.e("Utils", "initHapticConfig: Seems like no files or no permission to read files");
            return null;
        }
        for (File file : files) {
            try {
                fName = file.getName();
                String cfgName = fName.split("\\.")[0];
                FileInputStream fStream = new FileInputStream(new File(path + "/" + fName));
                String json = IOUtils.toString(fStream, StandardCharsets.UTF_8);
                HapticProfile pattern = new HapticProfile(json);
                PatternTranslator translator = new PatternTranslator(hand, buzz);
                translator.setHapticProfile(pattern);
                hapticOptions.put(cfgName, translator);
            } catch (IOException e) {
                Log.e("Utils", "initHaptic: Failed to load haptic profile from file " + fName, e);
            }
        }
        return hapticOptions;
    }
}

