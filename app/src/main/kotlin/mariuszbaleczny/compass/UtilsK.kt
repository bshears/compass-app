package mariuszbaleczny.compass

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.pawegio.kandroid.e

/**
 * Created by mariusz on 05.11.16.
 */
class UtilsK {

    companion object {
        fun convertRadiansToDegreesRounded(angleInRadians: Float): Int {
            return Math.round(((Math.toDegrees(angleInRadians.toDouble()) + ConstantsK.FULL_ANGLE) % ConstantsK.FULL_ANGLE).toInt().toFloat())
        }

        fun isCompassSensorPresent(context: Context): Boolean {
            val packageManager = context.packageManager
            return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)
        }

        /**
         * Source of location are GPS and NETWORK providers as it provides high accuracy
         */
        fun isLocationServicesEnabled(context: Context): Boolean {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                e(UtilsK.toString(), e.message.toString())
                return false
            }

        }

        fun hideKeyboard(v: View?, context: Context) {
            if (v != null) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            }
        }

        fun areGranted(grantResults: IntArray): Boolean {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) return false
            }
            return true
        }

        fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
            for (permission: String in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }

        fun isLatitudeInRange(value: Double): Boolean {
            return value >= ConstantsK.LATITUDE_MIN && value <= ConstantsK.LATITUDE_MAX
        }

        fun isLongitudeInRange(value: Double): Boolean {
            return value >= ConstantsK.LONGITUDE_MIN && value <= ConstantsK.LONGITUDE_MAX
        }
    }

}