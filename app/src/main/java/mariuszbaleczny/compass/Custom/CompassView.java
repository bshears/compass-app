package mariuszbaleczny.compass.custom;

import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import mariuszbaleczny.compass.Constants;

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
                Animation.RELATIVE_TO_SELF, Constants.ANIMATION_PIVOT_XY_VALUE,
                Animation.RELATIVE_TO_SELF, Constants.ANIMATION_PIVOT_XY_VALUE);
        ra.setInterpolator(new DecelerateInterpolator(Constants.ANIMATION_INTERPOLATOR_DECELERATION));
        ra.setDuration(Constants.ANIMATION_DURATION);
        ra.setFillAfter(true);

        currentNorthAngle = -angle;
        compassRose.startAnimation(ra);
    }

    public void rotateNeedle(float angle) {
        RotateAnimation ra = new RotateAnimation(
                currentLocationAngle,
                -angle,
                Animation.RELATIVE_TO_SELF, Constants.ANIMATION_PIVOT_XY_VALUE,
                Animation.RELATIVE_TO_SELF, Constants.ANIMATION_PIVOT_XY_VALUE);
        ra.setDuration(Constants.ANIMATION_DURATION);
        ra.setFillAfter(true);

        currentLocationAngle = -angle;
        compassNeedle.startAnimation(ra);
    }
}
