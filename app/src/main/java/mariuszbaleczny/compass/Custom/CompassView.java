package mariuszbaleczny.compass.Custom;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class CompassView {

    private final ImageView compassRose;
    private final ImageView compassNeedle;

    private float currentNorthAngle = 0f;
    private float currentLocationAngle = 0f;

    public CompassView(ImageView compassRose, ImageView compassNeedle) {
        this.compassRose = compassRose;
        this.compassNeedle = compassNeedle;
    }

    public void rotateRose(float angle) {
        RotateAnimation ra = new RotateAnimation(
                currentNorthAngle,
                -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(600);
        ra.setFillAfter(true);

        currentNorthAngle = -angle;
        compassRose.startAnimation(ra);
    }

    public void rotateNeedle(float angle) {
        RotateAnimation ra = new RotateAnimation(
                currentLocationAngle,
                -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(600);
        ra.setFillAfter(true);

        currentLocationAngle = -angle;
        compassNeedle.startAnimation(ra);
    }

    public float getCurrentNorthAngle() {
        return currentNorthAngle;
    }

    public void setCurrentNorthAngle(float currentNorthAngle) {
        this.currentNorthAngle = currentNorthAngle;
    }

    public float getCurrentLocationAngle() {
        return currentLocationAngle;
    }

    public void setCurrentLocationAngle(float currentLocationAngle) {
        this.currentLocationAngle = currentLocationAngle;
    }
}
