package com.shatilov.neobuzz.haptics;

import java.util.HashMap;
import java.util.Map;

public class PatternTranslator implements VibroTranslator {
    private static final Map<int[], int[][]> states2vibro = new HashMap<>();

    static {
        /*
        * The idea is to identify several key states and assign distinctive patterns
        * to them, similar to the training examples in the original NeoBuzz App
        * */
        states2vibro.put(new int[] {}, new int[][]{});
    }

    @Override
    public int[] vibrate() {
        return new int[]{0, 0, 0, 0};
    }
}
