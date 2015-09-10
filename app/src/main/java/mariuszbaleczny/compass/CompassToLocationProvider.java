package mariuszbaleczny.compass;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CompassToLocationProvider implements SensorEventListener, LocationListener {

    private final static String LOCATION_PROVIDER = "LocationProvider";
    private final static int MEASUREMENTS_COUNT = 3;

    private final Context context;
    private final LocationManager locationManager;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;

    private boolean providerStarted = false;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientationVector = new float[3];

    private List<Float> measurements = new ArrayList<>();
    private int measurementCounter = 0;
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;

    private Location myLocation = new Location(LOCATION_PROVIDER);
    private Location targetLocation;
    private GeomagneticField geomagneticField;

    private CompassToLocationListener compassToLocationListener;

    public CompassToLocationProvider(final Context context) {
        this.context = context;

        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.myLocation = location;

        Log.d(getClass().getName(), String.format("latitude: %f, longitude: %f", myLocation.getLatitude(),
                myLocation.getLongitude()));

        geomagneticField = new GeomagneticField(
                (float) this.myLocation.getLatitude(),
                (float) this.myLocation.getLongitude(),
                (float) this.myLocation.getAltitude(),
                myLocation.getTime());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.contains(context.getString(R.string.gps_provider))) {
            Log.d(getClass().getName(), provider + " : Location Services ON");
            compassToLocationListener.setLayoutElementsOnProvider(true);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.contains(context.getString(R.string.gps_provider))) {
            Log.d(getClass().getName(), provider + " : Location Services OFF");
            compassToLocationListener.setLayoutElementsOnProvider(false);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            lastAccelerometer = Utils.lowPassFilter(event.values.clone(), lastAccelerometer.clone(),
                    Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
            lastAccelerometerSet = true;
        } else if (event.sensor == magnetometer) {
            lastMagnetometer = Utils.lowPassFilter(event.values.clone(), lastMagnetometer.clone(),
                    Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
            lastMagnetometerSet = true;
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientationVector);
            float azimuthInRadians = orientationVector[0];

            float azimuthInDegrees = Utils.convertRadiansToDegreesRounded(azimuthInRadians);

            measurements.add(measurementCounter, azimuthInDegrees);
            measurementCounter++;

            if (measurementCounter == MEASUREMENTS_COUNT) {
                float northAngle = Utils.getMeasurementsAverage(MEASUREMENTS_COUNT, measurements);
                float locationAngle = 0f;
                if (geomagneticField != null) {
                    northAngle = northAngle + geomagneticField.getDeclination();
                    if (targetLocation != null) {
                        float bearing = myLocation.bearingTo(targetLocation);
                        locationAngle = northAngle - bearing;
                    }
                }
                compassToLocationListener.onCompassPointerRotate((int) northAngle, (int) locationAngle);
                measurementCounter = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setCompassToLocationListener(final CompassToLocationListener compassToLocationListener) {
        this.compassToLocationListener = compassToLocationListener;
    }

    public void setTargetLocation(Location location) {
        targetLocation = location;
    }

    public void resetTargetLocation() {
        targetLocation = null;
    }

    public void startIfNotStarted() {
        if (!providerStarted) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);

            for (final String provider : locationManager.getProviders(true)) {
                if (LocationManager.GPS_PROVIDER.equals(provider)
                        || LocationManager.PASSIVE_PROVIDER.equals(provider)
                        || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                    if (myLocation == null) {
                        myLocation = locationManager.getLastKnownLocation(provider);
                    }
                    locationManager.requestLocationUpdates(provider, 0, Constants.LOCATION_UPDATE_MIN_DISTANCE, this);
                }
            }
            setProviderStarted(true);
        }
    }

    public void stopIfStarted() {
        if (providerStarted) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
            locationManager.removeUpdates(this);
            setProviderStarted(false);
        }
    }

    public void setProviderStarted(boolean value) {
        providerStarted = value;
    }

    public interface CompassToLocationListener {
        void onCompassPointerRotate(int roseAngle, int needleAngle);

        void setLayoutElementsOnProvider(boolean enabled);
    }
}
