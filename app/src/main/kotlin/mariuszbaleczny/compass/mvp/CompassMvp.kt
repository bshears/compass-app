package mariuszbaleczny.compass.mvp

import android.support.annotation.Nullable
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import mariuszbaleczny.compass.location.CompassPointer
import mariuszbaleczny.compass.location.CompassPointer.CompassToLocationListener

/**
 * Created by mariusz on 21.11.16.
 */
interface CompassMvp {

    interface View {
        fun setCoordinateInputEnabled()
        fun setCoordinateInputDisabled()
        fun rotateCompass(roseAngle: Int, needleAngle: Int)
        fun setSubtitle(@StringRes resId: Int)
        fun setTitle(@StringRes resId: Int)
        fun onNullLatitude()
        fun onNullLongitude()
        fun onLatitudeInRange()
        fun onLatitudeOutOfRange()
        fun onLongitudeInRange()
        fun onLongitudeOutOfRange()
        fun asFragment(): Fragment
        fun onPresenterLoad(presenter: CompassPresenter)
        fun locationServicesCheckUp()
    }

    interface Presenter : CompassToLocationListener, BasePresenter {
        fun getView(): CompassMvp.View?
        fun bindView(view: View)
        fun setCompassPointer(compassPointer: CompassPointer)
        fun onLatitudeChanged(@Nullable latitude: Double?)
        fun onLongitudeChanged(@Nullable longitude: Double?)
    }

}