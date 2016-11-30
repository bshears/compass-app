package mariuszbaleczny.compass.location

import android.location.Location

/**
 * Created by mariusz on 23.11.16.
 */
interface CompassPointer {

    fun startIfNotStarted()
    fun stopIfStarted()
    fun setTargetLocation(location: Location?)
    fun setListener(listener: CompassToLocationListener)

    interface CompassToLocationListener {
        fun onCompassPointerRotate(roseAngle: Int, needleAngle: Int)
        fun onProviderEnabled(provider: String)
        fun onProviderDisabled(provider: String)
    }
}