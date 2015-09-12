package mariuszbaleczny.compass.location;

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

import mariuszbaleczny.compass.Constants;
import mariuszbaleczny.compass.R;
import mariuszbaleczny.compass.Utils;

public class CompassToLocationProvider implements SensorEventListener, LocationListener {

    private final static String LOCATION_PROVIDER = "LocationProvider";

    private final Context context;
    private final LocationManager locationManager;
    private final SensorManager sensorManager;
    private final Sensor sensor;

    private int northAngle = 0;
    private int targetLocationAngle = 0;

    private boolean providerStarted = false;
    private float[] rotationMatrix = new float[9];
    private float[] orientationVector = new float[3];

    private Location myLocation = new Location(LOCATION_PROVIDER);
    private Location targetLocation;
    private GeomagneticField geomagneticField;
    private CompassToLocationListener compassToLocationListener;

    public CompassToLocationProvider(final Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void setCompassToLocationListener(final CompassToLocationListener compassToLocationListener) {
        this.compassToLocationListener = compassToLocationListener;
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;

        Log.d(getClass().getName(), String.format("latitude: %f, longitude: %f", myLocation.getLatitude(),
                myLocation.getLongitude()));

        geomagneticField = new GeomagneticField(
                (float) myLocation.getLatitude(),
                (float) myLocation.getLongitude(),
                (float) myLocation.getAltitude(),
                myLocation.getTime());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(getClass().getName(), provider + " : Location Services ON");
        if (provider.contains(context.getString(R.string.gps_provider)) ||
                provider.contains(context.getString(R.string.network_provider))) {
            compassToLocationListener.setLayoutElementsOnProvider(true);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(getClass().getName(), provider + " : Location Services OFF");
        if (provider.contains(context.getString(R.string.gps_provider)) ||
                provider.contains(context.getString(R.string.network_provider))) {
            compassToLocationListener.setLayoutElementsOnProvider(false);
            setTargetLocation(null);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setTargetLocation(Location location) {
        targetLocation = location;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values.clone());
        float azimuthInRadians = SensorManager.getOrientation(rotationMatrix, orientationVector)[Constants.Z_AXIS_ROTATION];
        northAngle = Utils.convertRadiansToDegreesRounded(azimuthInRadians);
        updateAngle();
    }

    private void updateAngle() {
        calculateTrueNorthAngleIfPossible();
        calculateLocationAngleIfPossible();
        compassToLocationListener.onCompassPointerRotate(northAngle, targetLocationAngle);
    }

    private void calculateTrueNorthAngleIfPossible() {
        if (geomagneticField != null) {
            northAngle += geomagneticField.getDeclination();
        }
    }

    private void calculateLocationAngleIfPossible() {
        if (targetLocation != null && myLocation != null) {
            int bearing = (int) myLocation.bearingTo(targetLocation);
            targetLocationAngle = northAngle - bearing;
        } else {
            targetLocationAngle = 0;
        }
    }

    public void startIfNotStarted() {
        if (!providerStarted) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);

            for (final String provider : locationManager.getProviders(true)) {
                if (LocationManager.GPS_PROVIDER.equals(provider) ||
                        LocationManager.NETWORK_PROVIDER.equals(provider)) {
                    if (myLocation == null) {
                        myLocation = locationManager.getLastKnownLocation(provider);
                    }
                    locationManager.requestLocationUpdates(provider, Constants.MIN_UPDATE_INTERVAL_MS,
                            Constants.MIN_DISTANCE_UPDATE_IN_METERS, this);
                }
            }
            providerStarted = true;
        }
    }

    public void stopIfStarted() {
        if (providerStarted) {
            sensorManager.unregisterListener(this, sensor);
            locationManager.removeUpdates(this);
            providerStarted = false;
        }
    }

    public interface CompassToLocationListener {
        void onCompassPointerRotate(int roseAngle, int needleAngle);

        void setLayoutElementsOnProvider(boolean enabled);
    }
}
