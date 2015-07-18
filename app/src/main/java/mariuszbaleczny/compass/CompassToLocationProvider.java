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
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class CompassToLocationProvider implements SensorEventListener, LocationListener {

    private final static String TAG = "CompassLocationProvider";
    private final static String LOCATION_PROVIDER = "LocationProvider";
    private final static float ALPHA = 0.08f;
    private ChangeEventListener changeEventListener;
    private Context context;

    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private Location myLocation = new Location(LOCATION_PROVIDER);
    private GeomagneticField geomagneticField;

    private ArrayList<Float> measurements = new ArrayList<>();
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    private int measurementCounter = 0;
    private int numberOfMeasurements = 0;

    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private Toast infoToast;

    public CompassToLocationProvider(Context context) {
        this(context, 3);
    }

    public CompassToLocationProvider(Context context, int numberOfMeasurements) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        this.numberOfMeasurements = numberOfMeasurements;
    }

    public void setChangeEventListener(ChangeEventListener changeEventListener) {
        this.changeEventListener = changeEventListener;
    }

    public void start() {
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        for (final String provider : locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.PASSIVE_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                if (myLocation == null) {
                    myLocation = locationManager.getLastKnownLocation(provider);
                }
                locationManager.requestLocationUpdates(provider, 0, 100.0f, this);
            }
        }

        if(infoToast == null || infoToast.getView().getWindowVisibility() != View.VISIBLE) {
            infoToast = Toast.makeText(this.context, this.context.getString(R.string.calibration_info),
                    Toast.LENGTH_SHORT);
            infoToast.show();
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this, mAccelerometer);
        sensorManager.unregisterListener(this, mMagnetometer);
        locationManager.removeUpdates(this);
    }

    private float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private float getMeasurementAverage(int measurementsNumber, ArrayList arrayData) {
        float output = 0f;

        for (int i = 0; i < measurementsNumber; i++) {
            output += (float) arrayData.get(i);
        }
        return output / measurementsNumber;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.myLocation = location;
        geomagneticField = new GeomagneticField((float) this.myLocation.getLatitude(),
                (float) this.myLocation.getLongitude(),
                (float) this.myLocation.getAltitude(),
                System.currentTimeMillis());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Location Services ON");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Location Services OFF");
        if(infoToast == null || infoToast.getView().getWindowVisibility() != View.VISIBLE) {
            infoToast = Toast.makeText(context, context.getString(R.string.accuracy_info),
                    Toast.LENGTH_SHORT);
            infoToast.show();
            geomagneticField = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            mLastAccelerometer = lowPassFilter(event.values.clone(), mLastAccelerometer.clone());
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            mLastMagnetometer = lowPassFilter(event.values.clone(), mLastMagnetometer.clone());
            mLastMagnetometerSet = true;
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];

            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) % 360);
            azimuthInDegrees = Math.round(azimuthInDegrees);

            measurements.add(measurementCounter, azimuthInDegrees);

            if (measurementCounter == numberOfMeasurements) {
                double azimuth = getMeasurementAverage(numberOfMeasurements, measurements);

                if (geomagneticField != null) {
                    azimuth = azimuth + geomagneticField.getDeclination();
                    changeEventListener.onCompassToLocationChange(azimuth);
                } else {
                    azimuth = azimuthInDegrees;
                    changeEventListener.onCompassToLocationChange(azimuth);
                }

                measurementCounter = 0;
            } else {
                measurementCounter++;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public interface ChangeEventListener {
        void onCompassToLocationChange(double azimuth);
    }
}
