package mariuszbaleczny.compass.ui.helper

import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView

/**
 * Created by mariusz on 05.11.16.
 */
class CompassRotateHelper(private val compassRose: ImageView?, private val compassNeedle: ImageView?) {

    companion object {

        private val ANIMATION_PIVOT_XY_VALUE: Float = 0.5f
        private val ANIMATION_INTERPOLATOR_DECELERATION: Float = 10f
        private val ANIMATION_DURATION: Long = 1000

    }

    private var currentNorthAngle = 0
    private var currentLocationAngle = 0

    private fun getRotateAnimation(currentAngle: Int, angle: Int): RotateAnimation {
        val ra = RotateAnimation(
                currentAngle.toFloat(),
                (-angle).toFloat(),
                Animation.RELATIVE_TO_SELF, ANIMATION_PIVOT_XY_VALUE,
                Animation.RELATIVE_TO_SELF, ANIMATION_PIVOT_XY_VALUE)
        ra.interpolator = DecelerateInterpolator(ANIMATION_INTERPOLATOR_DECELERATION)
        ra.duration = ANIMATION_DURATION
        ra.fillAfter = true

        return ra
    }

    fun rotateRose(angle: Int) {
        val ra = getRotateAnimation(currentNorthAngle, angle)
        currentNorthAngle = -angle
        compassRose?.startAnimation(ra)
    }

    fun rotateNeedle(angle: Int) {
        val ra = getRotateAnimation(currentLocationAngle, angle)
        currentLocationAngle = -angle
        compassNeedle?.startAnimation(ra)
    }

}