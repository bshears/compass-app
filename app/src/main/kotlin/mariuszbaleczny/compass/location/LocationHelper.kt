package mariuszbaleczny.compass.location

import android.location.Location
import mariuszbaleczny.compass.Utils

/**
 * Created by mariusz on 05.11.16.
 */
class LocationHelper(private val location: Location) : CoordinateValidator {

    private var latitude: Double? = Double.NaN
    private var longitude: Double? = Double.NaN
    private var latitudeCorrect: Boolean = false
    private var longitudeCorrect: Boolean = false

    override fun getLocation(): Location? {
        if (isCorrect()) {
            location.latitude = latitude!!
            location.longitude = longitude!!
            return location
        }
        return null
    }

    override fun isCorrect(): Boolean {
        return latitudeCorrect && longitudeCorrect
    }

    override fun isLatitudeCorrect(): Boolean {
        return latitude != null && Utils.isLatitudeInRange(latitude!!)
    }

    override fun isLongitudeCorrect(): Boolean {
        return longitudeCorrect
    }

    override fun setLatitude(latitude: Double?) {
        if (latitude != null) {
            latitudeCorrect = Utils.isLatitudeInRange(latitude)
            this.latitude = latitude
        } else {
            this.latitude = null
            latitudeCorrect = false
        }
    }

    override fun setLongitude(longitude: Double?) {
        if (longitude != null) {
            longitudeCorrect = Utils.isLongitudeInRange(longitude)
            this.longitude = if (longitudeCorrect) longitude else Double.NaN
        } else {
            this.longitude = null
            longitudeCorrect = false
        }
    }

    override fun setIncorrect() {
        latitudeCorrect = false
        longitudeCorrect = false
    }

    override fun setIncorrectLatitude() {
        latitudeCorrect = false
    }

    override fun setIncorrectLongitude() {
        longitudeCorrect = false
    }

}