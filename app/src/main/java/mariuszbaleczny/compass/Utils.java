package mariuszbaleczny.compass;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

public class Utils {

    public static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) {
            return (input != null) ? input : new float[3];
        }

        try {
            for (int i = 0; i < input.length; i++) {
                output[i] = output[i] + Constants.ALPHA * (input[i] - output[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("Utils.java", e.getMessage());
        }
        return output;
    }

    public static float getMeasurementsAverage(int measurementsNumber, List data) {
        float output = 0f;

        if (data == null) {
            return output;
        }
        measurementsNumber = (measurementsNumber <= 0) ? 1 : measurementsNumber;

        for (int i = 0; i < measurementsNumber; i++) {
            output += (float) data.get(i);
        }

        return output / measurementsNumber;
    }

    public static boolean isCoordinateInRange(double coordinate, boolean latitude) {
        if (latitude) {
            return (coordinate >= Constants.LATITUDE_MIN && coordinate <= Constants.LATITUDE_MAX);
        } else {
            return (coordinate >= Constants.LONGITUDE_MIN && coordinate <= Constants.LONGITUDE_MAX);
        }
    }

    public static boolean isCompassSensorPresent(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    public static void hideKeyboard(View view, Context context) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static float convertRadiansToDegreesRounded(float angle) {
        return Math.round((float) ((Math.toDegrees(angle) + Constants.FULL_ANGLE) % Constants.FULL_ANGLE));
    }
}
