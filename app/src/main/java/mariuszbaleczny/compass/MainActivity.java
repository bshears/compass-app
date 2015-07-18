package mariuszbaleczny.compass;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity implements CompassToLocationProvider.ChangeEventListener {

    private static final int NUMBER_OF_MEASUREMENTS_FOR_SMOOTHING_DATA = 3;
    private CompassToLocationProvider compassToLocationProvider;

    private double currentAngle = 0d;
    private double targetLatitude = 0d;
    private double targetLongitude = 0d;

    private boolean isTargetLatitude = false;
    private boolean isTargetLongitude = false;

    private ImageView compassPointerView;

    private TextWatcher latitudeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() != 0) {
                targetLatitude = Double.parseDouble(s.toString());
                isTargetLatitude = true;
            } else {
                isTargetLatitude = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(isTargetLatitude & isTargetLongitude){
                compassToLocationProvider.setTargetLocationCoordinates(targetLatitude, targetLongitude);
            } else {
                compassToLocationProvider.resetTargetLocation();
            }
        }
    };
    private TextWatcher longitudeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() != 0) {
                targetLongitude = Double.parseDouble(s.toString());
                isTargetLongitude = true;
            } else {
                isTargetLongitude = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(isTargetLatitude & isTargetLongitude){
                compassToLocationProvider.setTargetLocationCoordinates(targetLatitude, targetLongitude);
            } else {
                compassToLocationProvider.resetTargetLocation();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassPointerView = (ImageView) findViewById(R.id.arrowPointer);
        EditText latitudeEditText = (EditText) findViewById(R.id.latitudeMainActivity);
        EditText longitudeEditText = (EditText) findViewById(R.id.longitudeMainActivity);

        latitudeEditText.addTextChangedListener(latitudeTextWatcher);
        longitudeEditText.addTextChangedListener(longitudeTextWatcher);

        compassToLocationProvider = new CompassToLocationProvider(this,
                NUMBER_OF_MEASUREMENTS_FOR_SMOOTHING_DATA);
        compassToLocationProvider.setChangeEventListener(this);
    }

    protected void onResume() {
        super.onResume();
        compassToLocationProvider.start();
    }

    protected void onPause() {
        super.onPause();
        compassToLocationProvider.stop();
    }

    private void animatePointer(double angle) {
        RotateAnimation ra = new RotateAnimation(
                (float) currentAngle,
                (float) -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);

        compassPointerView.startAnimation(ra);
    }

    @Override
    public void onCompassToLocationChange(double azimuth) {
        animatePointer(azimuth);
        currentAngle = -azimuth;
    }
}