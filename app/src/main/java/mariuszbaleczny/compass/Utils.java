package mariuszbaleczny.compass;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

public class Utils {

    public static float[] lowPassFilter(float[] input, float[] output, float smoothCoefficient) {
        float[] out;
        if (output == null) {
            return (input != null) ? input : new float[3];
        }
        if (input == null) {
            return output;
        }
        out = output.clone();

        try {
            for (int i = 0; i < input.length; i++) {
                out[i] = output[i] + smoothCoefficient * (input[i] - output[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(Utils.class.getName(), e.getMessage());
        }
        return out;
    }

    public static float getMeasurementsAverage(int measurementsCount, List<Float> data) {
        float output = 0f;

        if (data == null) {
            return output;
        }
        if (measurementsCount <= 0) {
            return 0f;
        }

        for (int i = 0; i < measurementsCount; i++) {
            output += data.get(i);
        }

        return output / measurementsCount;
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

    public static boolean isLocationServiceEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(context.getClass().getName(), e.getMessage());
            return false;
        }
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
