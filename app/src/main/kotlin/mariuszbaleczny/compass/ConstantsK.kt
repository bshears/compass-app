package mariuszbaleczny.compass

/**
 * Created by mariusz on 05.11.16.
 */
class ConstantsK {
    companion object {
        val Z_AXIS_ROTATION = 0

        val LATITUDE_MAX = 90.0
        val LATITUDE_MIN = -90.0
        val LONGITUDE_MAX = 180.0
        val LONGITUDE_MIN = -180.0

        val FULL_ANGLE = 360f
        val MIN_DISTANCE_UPDATE_IN_METERS = 10f
        private val SECOND_IN_MS: Long = 1000
        val MIN_UPDATE_INTERVAL_MS = 30 * SECOND_IN_MS

        val ON_BACK_PRESS_DELAY_TIME = 2000
    }
}