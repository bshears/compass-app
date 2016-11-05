package mariuszbaleczny.compass;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import kotlin.Deprecated;

public class Utils {

    @Deprecated(message = "Replaced with boilerplate-free kotlin language properties")
    public static boolean isCoordinateInRange(double coordinate, boolean latitude) {
        if (latitude) {
            return (coordinate >= Constants.LATITUDE_MIN && coordinate <= Constants.LATITUDE_MAX);
        } else {
            return (coordinate >= Constants.LONGITUDE_MIN && coordinate <= Constants.LONGITUDE_MAX);
        }
    }

    public static int convertRadiansToDegreesRounded(float angleInRadians) {
        return Math.round((int) ((Math.toDegrees(angleInRadians) + Constants.FULL_ANGLE) % Constants.FULL_ANGLE));
    }

    public static boolean isCompassSensorPresent(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    /**
     * Source of location are GPS and NETWORK providers as it provides high accuracy
     */
    public static boolean isLocationServicesEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        } catch (Exception e) {
            Log.e(context.getClass().getName(), e.getMessage());
            return false;
        }
    }

    public static void hideKeyboard(View view, Context context) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    public static boolean areGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    public static boolean isLatitudeInRange(double value) {
        return (value >= Constants.LATITUDE_MIN && value <= Constants.LATITUDE_MAX);
    }

    public static boolean isLongitudeInRange(double value) {
        return (value >= Constants.LONGITUDE_MIN && value <= Constants.LONGITUDE_MAX);
    }

}
