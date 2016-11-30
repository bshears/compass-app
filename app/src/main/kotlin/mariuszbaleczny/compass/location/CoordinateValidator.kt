package mariuszbaleczny.compass.location

import android.location.Location

/**
 * Created by mariusz on 23.11.16.
 */
interface CoordinateValidator {
    fun getLocation(): Location?
    fun isCorrect(): Boolean
    fun isLatitudeCorrect(): Boolean
    fun isLongitudeCorrect(): Boolean
    fun setLatitude(latitude: Double?)
    fun setLongitude(longitude: Double?)
    fun setIncorrect()
    fun setIncorrectLongitude()
    fun setIncorrectLatitude()
}