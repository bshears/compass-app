package mariuszbaleczny.compass.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.pawegio.kandroid.d
import mariuszbaleczny.compass.CompassActivityK
import mariuszbaleczny.compass.Constants
import mariuszbaleczny.compass.R
import mariuszbaleczny.compass.Utils

/**
 * Created by mariusz on 05.11.16.
 */
class CompassToLocationProviderK(private val context: Context) : SensorEventListener, LocationListener {

    companion object {
        val LOC_PERMISSION_ON_STOP_REQUEST_CODE = 101
        val LOC_PERMISSION_ON_START_REQUEST_CODE = 100

        private val LOCATION_PROVIDER = "LocationProvider"
        private val COMPASS_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private val sensorManager: SensorManager
    private val locationManager: LocationManager
    private val sensor: Sensor

    private var myLocation: Location = Location(LOCATION_PROVIDER)

    private val rotationMatrix = FloatArray(9)
    private val orientationVector = FloatArray(3)
    private var northAngle = 0
    private var targetLocationAngle = 0
    private var providerStarted = false

    private var targetLocation: Location? = null
    private var geomagneticField: GeomagneticField? = null
    private var compassToLocationListener: CompassToLocationListener? = null

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onLocationChanged(location: Location) {
        myLocation = location
        d(toString(), String.format("latitude: %f, longitude: %f", myLocation.latitude, myLocation.longitude))
        geomagneticField = GeomagneticField(
                myLocation.latitude.toFloat(),
                myLocation.longitude.toFloat(),
                myLocation.altitude.toFloat(),
                myLocation.time)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // TODO: implement actions
    }

    override fun onProviderEnabled(provider: String) {
        d(toString(), provider + " : Location Services ON")
        if (provider.contains(context.getString(R.string.gps_provider)) || provider.contains(context.getString(R.string.network_provider))) {
            compassToLocationListener?.setLayoutElementsOnProvider(true)
        }
    }

    override fun onProviderDisabled(provider: String) {
        d(toString(), provider + " : Location Services OFF")
        if (provider.contains(context.getString(R.string.gps_provider)) || provider.contains(context.getString(R.string.network_provider))) {
            compassToLocationListener?.setLayoutElementsOnProvider(false)
            setTargetLocation(null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // TODO: implement actions
    }

    override fun onSensorChanged(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values.clone())
        val azimuthInRadians = SensorManager.getOrientation(rotationMatrix, orientationVector)[Constants.Z_AXIS_ROTATION]
        northAngle = Utils.convertRadiansToDegreesRounded(azimuthInRadians)
        updateAngle()
    }

    fun setTargetLocation(location: Location?) {
        targetLocation = location
    }

    fun setCompassToLocationListener(listener: CompassToLocationListener) {
        compassToLocationListener = listener
    }

    private fun updateAngle() {
        calculateTrueNorthAngleIfPossible()
        calculateLocationAngleIfPossible()
        compassToLocationListener?.onCompassPointerRotate(northAngle, targetLocationAngle)
    }

    private fun calculateTrueNorthAngleIfPossible() {
        northAngle += geomagneticField?.declination?.toInt() as Int
    }

    private fun calculateLocationAngleIfPossible() {
        if (targetLocation != null) {
            val bearing = myLocation.bearingTo(targetLocation).toInt()
            targetLocationAngle = northAngle.toInt() - bearing.toInt()
        } else {
            targetLocationAngle = 0
        }
    }

    fun startIfNotStarted() {
        if (!providerStarted) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)

            if (ActivityCompat.checkSelfPermission(context, COMPASS_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, COMPASS_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as CompassActivityK, COMPASS_PERMISSIONS, LOC_PERMISSION_ON_START_REQUEST_CODE)
                return
            }

            for (provider in locationManager.getProviders(true)) {
                if (LocationManager.GPS_PROVIDER == provider || LocationManager.NETWORK_PROVIDER == provider) {
                    myLocation = locationManager.getLastKnownLocation(provider)
                    locationManager.requestLocationUpdates(provider, Constants.MIN_UPDATE_INTERVAL_MS,
                            Constants.MIN_DISTANCE_UPDATE_IN_METERS, this)
                }
            }
            providerStarted = true
        }
    }

    fun stopIfStarted() {
        if (providerStarted) {
            if (ActivityCompat.checkSelfPermission(context, COMPASS_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, COMPASS_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            sensorManager.unregisterListener(this, sensor)
            locationManager.removeUpdates(this)
            providerStarted = false
        }
    }

    interface CompassToLocationListener {
        fun onCompassPointerRotate(roseAngle: Int, needleAngle: Int)

        fun setLayoutElementsOnProvider(enabled: Boolean)
    }

}