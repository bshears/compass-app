package mariuszbaleczny.compass;

import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class CompassView {

    private final ImageView compassRose;
    private final ImageView compassNeedle;

    private int currentNorthAngle = 0;
    private int currentLocationAngle = 0;

    public CompassView(ImageView compassRose, ImageView compassNeedle) {
        this.compassRose = compassRose;
        this.compassNeedle = compassNeedle;
    }

    public void rotateRose(int angle) {
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

    public void rotateNeedle(int angle) {
        RotateAnimation ra = new RotateAnimation(
                currentLocationAngle,
                -angle,
                Animation.RELATIVE_TO_SELF, Constants.ANIMATION_PIVOT_XY_VALUE,
                Animation.RELATIVE_TO_SELF, Constants.ANIMATION_PIVOT_XY_VALUE);
        ra.setInterpolator(new DecelerateInterpolator(Constants.ANIMATION_INTERPOLATOR_DECELERATION));
        ra.setDuration(Constants.ANIMATION_DURATION);
        ra.setFillAfter(true);

        currentLocationAngle = -angle;
        compassNeedle.startAnimation(ra);
    }

}
