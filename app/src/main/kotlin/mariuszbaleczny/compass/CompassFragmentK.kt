package mariuszbaleczny.compass

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.pawegio.kandroid.e
import com.pawegio.kandroid.find
import com.pawegio.kandroid.textWatcher
import mariuszbaleczny.compass.custom.CustomEditText
import mariuszbaleczny.compass.location.CompassToLocationProvider
import mariuszbaleczny.compass.location.LocationHelper

/**
 * Created by mariusz on 03.11.16.
 */
class CompassFragmentK : Fragment(), CompassToLocationProvider.CompassToLocationListener {

    companion object {
        const val COMPASS_APPLICATION: String = "compass_location_provider"
        const val REQUEST_CODE_SETTINGS: Int = 0
        val TAG: String = "CompassFragmentK"

        fun newInstance(): CompassFragmentK {
            return CompassFragmentK()
        }
    }

    private var locationHelper: LocationHelper? = null
    private var compassLocationProvider: CompassToLocationProvider? = null

    private var compassView: CompassView? = null
    private var title: TextView? = null
    private var subtitle: TextView? = null
    private var latitude: CustomEditText? = null
    private var longitude: CustomEditText? = null
    private var latitudeLayout: TextInputLayout? = null
    private var longitudeLayout: TextInputLayout? = null

    private var askedAfterStart: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (UtilsK.isCompassSensorPresent(context)) {
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
        if (!askedAfterStart) {
            checkUpAndSetupLocationServices()
            askedAfterStart = true
        }
    }

    override fun onPause() {
        super.onPause()
        compassLocationProvider?.stopIfStarted()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true
        askedAfterStart = false
    }

    override fun onCompassPointerRotate(roseAngle: Int, needleAngle: Int) {
        compassView?.rotateRose(roseAngle)
        compassView?.rotateNeedle(needleAngle)
    }

    override fun setLayoutElementsOnProvider(enabled: Boolean) {
        setCoordinatesEnabled(enabled)
        if (enabled) {
            setSubtitle(R.string.info_text_subtitle, null)
            if (locationHelper?.isCorrect as Boolean) {
                compassLocationProvider?.setTargetLocation(locationHelper?.location)
                setTitle(R.string.point_location_title, Color.BLACK)
            }
        } else {
            setTitle(R.string.point_north_title, Color.BLACK)
            setSubtitle(R.string.touch_info_error_subtitle, View.OnClickListener { setupLayoutOnLocationServicesCheckUp() })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CompassToLocationProvider.LOC_PERMISSION_ON_START_REQUEST_CODE -> {
                if (UtilsK.areGranted(grantResults)) {
                    compassLocationProvider?.startIfNotStarted()
                }
            }
            CompassToLocationProvider.LOC_PERMISSION_ON_STOP_REQUEST_CODE -> {
                if (UtilsK.areGranted(grantResults)) {
                    compassLocationProvider?.stopIfStarted()
                }
            }
        }
    }

    private fun setupLayoutOnLocationServicesCheckUp() {
        if (UtilsK.isLocationServicesEnabled(context)) {
            setTitle(R.string.point_north_title, Color.BLACK)
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

    private fun onLatitudeChanged(value: Double) {
        if (Double.NaN == value) {
            changeUiOnInvalidLatitude()
            resetNeedlePosition()
            return
        }

        locationHelper?.setLatitude(value)

        // even when coordinate is out its of range, then target location will be set to null (reset)
        compassLocationProvider?.setTargetLocation(locationHelper?.location)

        if (locationHelper?.isCorrect as Boolean) {
            setTitle(R.string.point_location_title, Color.BLACK)
            clearLatitudeOutOfRangeError()
        }

        // incorrect location doesn't mean that input value is incorrect!
        if (!UtilsK.isLatitudeInRange(value)) {
            setTitle(R.string.point_north_title, Color.BLACK)
            setLatitudeOutOfRangeError()
        }
    }

    private fun onLongitudeChanged(value: Double) {
        if (Double.NaN == value) {
            changeUiOnInvalidLongitude()
            resetNeedlePosition()
            return
        }

        locationHelper?.setLongitude(value)

        // even when coordinate is out its of range, then target location will be set to null (reset)
        compassLocationProvider?.setTargetLocation(locationHelper?.location)

        if (locationHelper?.isCorrect as Boolean) {
            setTitle(R.string.point_location_title, Color.BLACK)
            clearLongitudeOutOfError()
        }

        // incorrect location doesn't mean that input value is incorrect!
        if (!UtilsK.isLongitudeInRange(value)) {
            setTitle(R.string.point_north_title, Color.BLACK)
            setLongitudeOutOfRangeError()
        }
    }

    private fun changeUiOnInvalidLongitude() {
        clearLongitudeOutOfError()
        setTitle(R.string.point_north_title, Color.BLACK)
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
        setTitle(R.string.point_north_title, Color.BLACK)
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
        compassView = CompassView(rose, needle)

        rose?.setOnClickListener { checkUpAndSetupLocationServices() }
        needle?.setOnClickListener { checkUpAndSetupLocationServices() }
    }

    private fun setupCompassToLocationProvider() {
        compassLocationProvider = CompassToLocationProvider(context)
        compassLocationProvider?.setCompassToLocationListener(this)
    }

    private fun setupCoordinateLayouts() {
        if (UtilsK.isCompassSensorPresent(context)) {
            latitude?.setOnEditorActionListener({ v, id, event -> onEditorAction(longitude, v, id) })
            longitude?.setOnEditorActionListener({ v, id, event -> onEditorAction(latitude, v, id) })
            latitude?.textWatcher { afterTextChanged { s -> onLatitudeChanged(s) } }
            longitude?.textWatcher { afterTextChanged { s -> onChangedLongitude(s) } }
        }
    }

    private fun onEditorAction(complementaryField: CustomEditText?, v: TextView?, actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            controlFocus(complementaryField, v as View)
            return false
        }
        return true
    }

    private fun controlFocus(field: CustomEditText?, v: View) {
        if (!(field?.text?.isEmpty() as Boolean)) {
            UtilsK.hideKeyboard(v, context)
            v.clearFocus()
        } else {
            field?.requestFocus()
        }
    }

    private fun onChangedLongitude(s: Editable?) {
        onLongitudeChanged(parseDouble(s))
    }

    private fun onLatitudeChanged(s: Editable?) {
        onLatitudeChanged(parseDouble(s))
    }

    private fun parseDouble(s: Editable?): Double {
        try {
            return parseDouble(s)
        } catch (ex: NumberFormatException) {
            e(tag, ex.message as String)
            return Double.NaN
        }
    }

    private fun setCoordinatesEnabled(enabled: Boolean) {
        latitude?.isEnabled = enabled
        longitude?.isEnabled = enabled
    }

    private fun setupTitleAndSubtitle() {
        if (!UtilsK.isCompassSensorPresent(context)) {
            title?.setText(R.string.compass_not_detected_title)
        } else {
            title?.setText(R.string.point_north_title)
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
        if (UtilsK.isCompassSensorPresent(activity)) {
            setupLayoutOnLocationServicesCheckUp()
        }
    }

}