package mariuszbaleczny.compass;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity implements CompassToLocationProvider.ChangeEventListener {

    private static final int NUMBER_OF_MEASUREMENTS_FOR_SMOOTHING_DATA = 3;
    private CompassToLocationProvider compassToLocationProvider;

    public ImageView compassPointerView;
    private double currentAngle = 0d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassPointerView = (ImageView) findViewById(R.id.arrowPointer);

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