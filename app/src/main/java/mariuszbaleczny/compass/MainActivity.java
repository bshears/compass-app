package mariuszbaleczny.compass;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    private static final float ALPHA = 0.1f;
    private static final int NUMBER_OF_MEASUREMENTS = 5;
    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private int measurementCounter = 0;
    private ArrayList<Float> measurements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.arrowPointer);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        Toast.makeText(this, getString(R.string.calibration_info), Toast.LENGTH_SHORT).show();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    private float[] applyLowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            mLastAccelerometer = applyLowPassFilter(event.values.clone(), mLastAccelerometer.clone());
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            mLastMagnetometer = applyLowPassFilter(event.values.clone(), mLastMagnetometer.clone());
            mLastMagnetometerSet = true;
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];

            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            azimuthInDegrees = Math.round(azimuthInDegrees);
            measurements.add(measurementCounter, azimuthInDegrees);

            if (measurementCounter == NUMBER_OF_MEASUREMENTS) {
                float out = getMeasurementAverage(NUMBER_OF_MEASUREMENTS, measurements);
                animatePointer(out);
                mCurrentDegree = -out;
                measurementCounter = 0;
            } else {
                measurementCounter++;
            }
        }
    }

    private void animatePointer(float measurement) {
        RotateAnimation ra = new RotateAnimation(
                mCurrentDegree,
                -measurement,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);

        mPointer.startAnimation(ra);
    }

    private float getMeasurementAverage(int measurementsNumber, ArrayList arrayData) {
        float output = 0f;

        for (int i = 0; i < measurementsNumber; i++) {
            output += (float) arrayData.get(i);
        }
        return output / measurementsNumber;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}