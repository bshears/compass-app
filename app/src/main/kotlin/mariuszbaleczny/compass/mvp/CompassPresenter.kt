package mariuszbaleczny.compass.mvp

import android.location.Location
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import mariuszbaleczny.compass.R
import mariuszbaleczny.compass.location.CompassPointer
import mariuszbaleczny.compass.location.CoordinateValidator
import mariuszbaleczny.compass.location.LocationHelper
import mariuszbaleczny.compass.mvp.CompassMvp.View
import mariuszbaleczny.compass.ui.fragment.CompassFragment.Companion.COMPASS_APPLICATION
import java.util.HashMap

/**
 * Created by mariusz on 21.11.16.
 */
class CompassPresenter : CompassMvp.Presenter {

    var locationHelper: CoordinateValidator

    private var view: CompassMvp.View? = null

    private var compassPointer: CompassPointer? = null
    private var locationProviders: HashMap<String, Boolean>

    init {
        locationProviders = HashMap()
        locationProviders.put(GPS_PROVIDER, false)
        locationProviders.put(NETWORK_PROVIDER, false)
        locationHelper = LocationHelper(Location(COMPASS_APPLICATION))
    }

    override fun bindView(view: View) {
        this.view = view
    }

    override fun setCompassPointer(compassPointer: CompassPointer) {
        this.compassPointer = compassPointer
        this.compassPointer?.setListener(this)
    }

    override fun getView(): CompassMvp.View? {
        return view
    }

    override fun onCompassPointerRotate(roseAngle: Int, needleAngle: Int) {
        view?.rotateCompass(roseAngle, needleAngle)
    }

    override fun onProviderEnabled(provider: String) {
        onProviderEvent(provider, true)
    }

    override fun onProviderDisabled(provider: String) {
        onProviderEvent(provider, false)
    }

    override fun onLatitudeChanged(latitude: Double?) {
        locationHelper.setLatitude(latitude)
        compassPointer?.setTargetLocation(locationHelper.getLocation())

        setupTitles()

        if (latitude == null) {
            locationHelper.setIncorrectLatitude()
            view?.onNullLatitude()
            return
        }

        if (locationHelper.isLatitudeCorrect()) {
            view?.onLatitudeInRange()
        } else {
            view?.onLatitudeOutOfRange()
        }
    }

    override fun onLongitudeChanged(longitude: Double?) {
        locationHelper.setLongitude(longitude)
        compassPointer?.setTargetLocation(locationHelper.getLocation())

        setupTitles()

        if (longitude == null) {
            locationHelper.setIncorrectLongitude()
            view?.onNullLongitude()
            return
        }

        if (locationHelper.isLongitudeCorrect()) {
            view?.onLongitudeInRange()
        } else {
            view?.onLongitudeOutOfRange()
        }
    }

    private fun setupTitles() {
        if (locationHelper.isCorrect()) {
            view?.setTitle(R.string.point_location_title)
            view?.setSubtitle(R.string.info_text_subtitle)
        } else {
            view?.setTitle(R.string.needle_free_mode)
        }
    }

    private fun onProviderEvent(provider: String, enabled: Boolean) {
        if (locationProviders.containsKey(provider)) {
            locationProviders.put(provider, enabled)
            providersCheckUp()
        }
    }

    private fun providersCheckUp() {
        if (anyProvidersEnabled()) {
            onEnabledProviders()
        } else {
            onDisabledProviders()
        }
    }

    private fun anyProvidersEnabled() = locationProviders.containsValue(true)

    private fun onEnabledProviders() {
        view?.setCoordinateInputEnabled()
        view?.setSubtitle(R.string.info_text_subtitle)
        if (locationHelper.isCorrect()) {
            view?.setTitle(R.string.point_location_title)
            compassPointer?.setTargetLocation(locationHelper.getLocation())
        }
    }

    private fun onDisabledProviders() {
        view?.setCoordinateInputDisabled()
        view?.setTitle(R.string.needle_free_mode)
        view?.setSubtitle(R.string.touch_info_error_subtitle)
        view?.locationServicesCheckUp()
    }

}
