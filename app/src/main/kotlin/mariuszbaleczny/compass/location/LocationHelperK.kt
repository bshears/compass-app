package mariuszbaleczny.compass.location

import android.location.Location
import mariuszbaleczny.compass.UtilsK

/**
 * Created by mariusz on 05.11.16.
 */
class LocationHelperK(private val location: Location) {

    private var latitude: Double = Double.NaN
    private var longitude: Double = Double.NaN
    private var latitudeCorrect: Boolean = false
    private var longitudeCorrect: Boolean = false

    fun getLocation(): Location? {
        if (isCorrect()) {
            location.latitude = latitude
            location.longitude = longitude
            return location
        }
        return null
    }

    fun isCorrect(): Boolean {
        return latitudeCorrect && longitudeCorrect
    }

    fun setLatitude(latitude: Double?) {
        if (latitude != null) {
            latitudeCorrect = UtilsK.isLatitudeInRange(latitude)
            this.latitude = if (latitudeCorrect) latitude else java.lang.Double.NaN
        }
    }

    fun setLongitude(longitude: Double?) {
        if (longitude != null) {
            longitudeCorrect = UtilsK.isLongitudeInRange(longitude)
            this.longitude = if (longitudeCorrect) longitude else java.lang.Double.NaN
        }
    }

}