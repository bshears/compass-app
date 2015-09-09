package mariuszbaleczny.compass;

import java.util.ArrayList;

public class Utils {

    private final static float ALPHA = 0.08f;

    public static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public static float getMeasurementsAverage(int measurementsNumber, ArrayList dataArray) {
        float output = 0f;

        for (int i = 0; i < measurementsNumber; i++) {
            output += (float) dataArray.get(i);
        }
        return output / measurementsNumber;
    }
}
