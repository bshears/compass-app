package mariuszbaleczny.compass

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.pawegio.kandroid.d
import com.pawegio.kandroid.find
import com.pawegio.kandroid.textWatcher
import mariuszbaleczny.compass.custom.CustomEditText
import mariuszbaleczny.compass.location.CompassToLocationProvider
import mariuszbaleczny.compass.location.LocationHelper

/**
 * Created by mariusz on 03.11.16.
 */
class CompassFragment : Fragment(), CompassToLocationProvider.CompassToLocationListener {

    companion object {
        const val COMPASS_APPLICATION: String = "compass_location_provider"
        const val REQUEST_CODE_SETTINGS: Int = 0
        val TAG: String = "CompassFragment"

        fun newInstance(): CompassFragment {
            return CompassFragment()
        }
    }

    private var locationHelper: LocationHelper? = null
    private var compassLocationProvider: CompassToLocationProvider? = null

    private var compassView: CompassRotateHelper? = null
    private var title: TextView? = null
    private var subtitle: TextView? = null
    private var latitude: CustomEditText? = null
    private var longitude: CustomEditText? = null
    private var latitudeLayout: TextInputLayout? = null
    private var longitudeLayout: TextInputLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Utils.isCompassSensorPresent(context)) {
            setupCompassToLocationProvider()
            locationHelper = LocationHelper(Location(COMPASS_APPLICATION))
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View? = inflater?.inflate(R.layout.fragment_compass, container, false)
        setupLayoutWidgets(v)
        setupTitleAndSubtitle()
        setupCompassView(v)
        setupCoordinateLayouts()
        setCoordinatesEnabled(false)
        return v
    }

    override fun onResume() {
        super.onResume()
        checkUpAndSetupLocationServices()
    }

    override fun onPause() {
        super.onPause()
        compassLocationProvider?.stopIfStarted()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true
    }

    override fun onCompassPointerRotate(roseAngle: Int, needleAngle: Int) {
        compassView?.rotateRose(roseAngle)
        compassView?.rotateNeedle(needleAngle)
    }

    override fun setLayoutElementsOnProvider(enabled: Boolean) {
        setCoordinatesEnabled(enabled)
        if (enabled) {
            setSubtitle(R.string.info_text_subtitle, null)
            if (locationHelper?.isCorrect() as Boolean) {
                compassLocationProvider?.setTargetLocation(locationHelper?.getLocation())
                setTitle(R.string.point_location_title, Color.BLACK)
            }
        } else {
            setTitle(R.string.needle_free_mode, Color.BLACK)
            setSubtitle(R.string.touch_info_error_subtitle, View.OnClickListener { setupLayoutOnLocationServicesCheckUp() })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CompassToLocationProvider.LOC_PERMISSION_ON_START_REQUEST_CODE -> {
                if (Utils.areGranted(grantResults)) {
                    compassLocationProvider?.startIfNotStarted()
                } else {
                    setCoordinatesEnabled(false)
                }
            }
        }
    }

    private fun setupLayoutOnLocationServicesCheckUp() {
        if (Utils.isLocationServicesEnabled(context)) {
            setTitle(R.string.needle_free_mode, Color.BLACK)
            compassLocationProvider?.startIfNotStarted()
            setLayoutElementsOnProvider(true)
        } else {
            setTitle(R.string.empty, Color.BLACK)
            compassLocationProvider?.stopIfStarted()
            setLayoutElementsOnProvider(false)
            buildAndShowLocationServicesDialog()
        }
    }

    private fun buildAndShowLocationServicesDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        builder.setTitle(R.string.title_alert_dialog)
                .setMessage(R.string.message_alert_dialog)
                .setPositiveButton(R.string.positive_alert_dialog,
                        { dialogInterface, i ->
                            startActivityForResult(
                                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                    REQUEST_CODE_SETTINGS
                            )
                        })
                .setNegativeButton(R.string.negative_alert_dialog, null)
                .show()
    }

    private fun onLatitudeChanged(value: Double?) {
        if (value == null) {
            locationHelper?.setIncorrect()
            changeUiOnInvalidLatitude()
            resetNeedlePosition()
            return
        }

        locationHelper?.setLatitude(value)

        // even when coordinate is out its of range, then target location will be set to null (reset)
        compassLocationProvider?.setTargetLocation(locationHelper?.getLocation())

        // incorrect location doesn't mean that input value is incorrect!
        if (Utils.isLatitudeInRange(value)) {
            clearLatitudeOutOfRangeError()
        } else {
            setTitle(R.string.needle_free_mode, Color.BLACK)
            setLatitudeOutOfRangeError()
        }
    }

    private fun onLongitudeChanged(value: Double?) {
        if (value == null) {
            locationHelper?.setIncorrect()
            changeUiOnInvalidLongitude()
            resetNeedlePosition()
            return
        }

        locationHelper?.setLongitude(value)

        // even when coordinate is out its of range, then target location will be set to null (reset)
        compassLocationProvider?.setTargetLocation(locationHelper?.getLocation())

        // incorrect location doesn't mean that input value is incorrect!
        if (Utils.isLongitudeInRange(value)) {
            clearLongitudeOutOfError()
        } else {
            setTitle(R.string.needle_free_mode, Color.BLACK)
            setLongitudeOutOfRangeError()
        }
    }

    private fun changeUiOnInvalidLongitude() {
        clearLongitudeOutOfError()
        setTitle(R.string.needle_free_mode, Color.BLACK)
    }

    private fun clearLongitudeOutOfError() {
        longitudeLayout?.error = null
        longitudeLayout?.isErrorEnabled = false
    }

    private fun resetNeedlePosition() {
        // set null location, so needle will reset its state
        compassLocationProvider?.setTargetLocation(null)
    }

    private fun changeUiOnInvalidLatitude() {
        clearLatitudeOutOfRangeError()
        setTitle(R.string.needle_free_mode, Color.BLACK)
    }

    private fun clearLatitudeOutOfRangeError() {
        latitudeLayout?.error = null
        latitudeLayout?.isErrorEnabled = false
    }

    private fun setTitle(resId: Int, color: Int) {
        title?.setText(resId)
        title?.setTextColor(color)
    }

    private fun setSubtitle(resId: Int, listener: View.OnClickListener?) {
        subtitle?.setText(resId)
        subtitle?.setOnClickListener(listener)
    }

    private fun setupLayoutWidgets(v: View?) {
        title = v?.find<TextView>(R.id.fragment_compass_title_text_view)
        subtitle = v?.find<TextView>(R.id.fragment_compass_subtitle_text_view)
        latitude = v?.find<CustomEditText>(R.id.fragment_compass_latitude_edit_text)
        longitude = v?.find<CustomEditText>(R.id.fragment_compass_longitude_edit_text)
        latitudeLayout = v?.find<TextInputLayout>(R.id.fragment_compass_latitude_text_input)
        longitudeLayout = v?.find<TextInputLayout>(R.id.fragment_compass_longitude_text_input)
    }

    private fun setupCompassView(v: View?) {
        val needle: ImageView? = v?.find<ImageView>(R.id.fragment_compass_needle)
        val rose: ImageView? = v?.find<ImageView>(R.id.fragment_compass_rose)
        compassView = CompassRotateHelper(rose, needle)

        rose?.setOnClickListener { requestPermissionsIfNotGranted() }
        needle?.setOnClickListener { requestPermissionsIfNotGranted() }
    }

    private fun requestPermissionsIfNotGranted() {
        if (!Utils.arePermissionsGranted(context, CompassToLocationProvider.COMPASS_PERMISSIONS)) {
            ActivityCompat.requestPermissions(context as CompassActivity,
                    CompassToLocationProvider.COMPASS_PERMISSIONS,
                    CompassToLocationProvider.LOC_PERMISSION_ON_START_REQUEST_CODE)
        }
    }

    private fun setupCompassToLocationProvider() {
        compassLocationProvider = CompassToLocationProvider(context)
        compassLocationProvider?.setCompassToLocationListener(this)
    }

    private fun setupCoordinateLayouts() {
        if (Utils.isCompassSensorPresent(context)) {
            latitude?.setOnEditorActionListener({ v, id, event -> onCoordinateFieldAction(longitude, v, id) })
            longitude?.setOnEditorActionListener({ v, id, event -> onCoordinateFieldAction(latitude, v, id) })
            latitude?.textWatcher { afterTextChanged { s -> onCoordinateChanged() } }
            longitude?.textWatcher { afterTextChanged { s -> onCoordinateChanged() } }
        }
    }

    private fun onCoordinateFieldAction(complementaryField: CustomEditText?, v: TextView?, actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            controlFocus(complementaryField, v as View)
            return false
        }
        return true
    }

    private fun controlFocus(field: CustomEditText?, v: View) {
        if (!(field?.text?.isEmpty() as Boolean)) {
            Utils.hideKeyboard(v, context)
            v.clearFocus()
        } else {
            field?.requestFocus()
        }
    }

    private fun onCoordinateChanged() {
        val latitude: Double? = parseDouble(this.latitude?.text)
        val longitude: Double? = parseDouble(this.longitude?.text)

        onLatitudeChanged(latitude)
        onLongitudeChanged(longitude)

        if (locationHelper?.isCorrect() as Boolean) {
            setTitle(R.string.point_location_title, Color.BLACK)
            clearLatitudeOutOfRangeError()
            clearLongitudeOutOfError()
        }
    }

    private fun parseDouble(s: Editable?): Double? {
        try {
            return s.toString().toDouble()
        } catch (ex: NumberFormatException) {
            d(tag, ex.message as String)
            return null
        }
    }

    private fun setCoordinatesEnabled(enabled: Boolean) {
        latitude?.isEnabled = enabled
        longitude?.isEnabled = enabled
    }

    private fun setupTitleAndSubtitle() {
        if (!Utils.isCompassSensorPresent(context)) {
            title?.setText(R.string.compass_not_detected_title)
        } else {
            title?.setText(R.string.needle_free_mode)
        }
        title?.setText(R.string.empty)
    }

    private fun setLatitudeOutOfRangeError() {
        latitudeLayout?.error = getString(R.string.error_latitude_out_of_range)
    }

    private fun setLongitudeOutOfRangeError() {
        longitudeLayout?.error = getString(R.string.error_longitude_out_of_range)
    }

    private fun checkUpAndSetupLocationServices() {
        if (Utils.isCompassSensorPresent(activity)) {
            setupLayoutOnLocationServicesCheckUp()
        }
    }

}