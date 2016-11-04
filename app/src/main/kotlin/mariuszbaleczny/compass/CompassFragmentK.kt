package mariuszbaleczny.compass

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import mariuszbaleczny.compass.custom.CustomEditText
import mariuszbaleczny.compass.custom.CustomEditTextActionEditor
import mariuszbaleczny.compass.custom.CustomEditTextWatcher
import mariuszbaleczny.compass.location.CompassToLocationProvider
import mariuszbaleczny.compass.location.LocationHelper

/**
 * Created by mariusz on 03.11.16.
 */
class CompassFragmentK : Fragment(), CompassToLocationProvider.CompassToLocationListener,
        CustomEditTextWatcher.OnCoordinateChangeListener {

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
        if (Utils.isCompassSensorPresent(context)) {
            setupCompassToLocationProvider()
            locationHelper = LocationHelper(Location(COMPASS_APPLICATION))
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater?.inflate(R.layout.fragment_compass, container, false)

        setupLayoutWidgets(view)
        setupTitleAndSubtitle()
        setupCompassView(view)
        setupCoordinateLayouts()
        setCoordinatesEnabled(false)

        return view
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
                if (Utils.areGranted(grantResults)) {
                    compassLocationProvider?.startIfNotStarted()
                }
            }
            CompassToLocationProvider.LOC_PERMISSION_ON_STOP_REQUEST_CODE -> {
                if (Utils.areGranted(grantResults)) {
                    compassLocationProvider?.stopIfStarted()
                }
            }
        }
    }

    private fun setupLayoutOnLocationServicesCheckUp() {
        if (Utils.isLocationServicesEnabled(context)) {
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

    override fun onCoordinateChanged(latitude: Boolean, coordinate: Double) {
        // when coordinate is NaN (i.e. when empty EditText)
        if (java.lang.Double.isNaN(coordinate)) {
            clearCoordinateInputOutOfRangeError(latitude)
            setTitle(R.string.point_north_title, Color.BLACK)
            // set null location, so needle will reset its state
            compassLocationProvider?.setTargetLocation(null)
            return
        }

        if (latitude) {
            locationHelper?.setLatitude(coordinate)
        } else {
            locationHelper?.setLongitude(coordinate)
        }
        // even when coordinate is out its of range, then target location will be set to null (reset)
        compassLocationProvider?.setTargetLocation(locationHelper?.location)

        if (locationHelper?.isCorrect as Boolean) {
            setTitle(R.string.point_location_title, Color.BLACK)
            clearCoordinateInputOutOfRangeError(latitude)
        }
        // incorrect location doesn't mean that input value is incorrect!
        if (!Utils.isCoordinateInRange(coordinate, latitude)) {
            onEmptyOrWrongInput(latitude, true)
        }
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
        title = v?.findViewById(R.id.fragment_compass_title_text_view) as TextView
        subtitle = v?.findViewById(R.id.fragment_compass_subtitle_text_view) as TextView
        latitude = v?.findViewById(R.id.fragment_compass_latitude_edit_text) as CustomEditText
        longitude = v?.findViewById(R.id.fragment_compass_longitude_edit_text) as CustomEditText
        latitudeLayout = v?.findViewById(R.id.fragment_compass_latitude_text_input) as TextInputLayout
        longitudeLayout = v?.findViewById(R.id.fragment_compass_longitude_text_input) as TextInputLayout
    }

    private fun setupCompassView(v: View?) {
        val needle: ImageView? = v?.findViewById(R.id.fragment_compass_needle) as ImageView
        val rose: ImageView? = v?.findViewById(R.id.fragment_compass_rose) as ImageView
        compassView = CompassView(rose, needle)

        val clickListener: View.OnClickListener = View.OnClickListener { checkUpAndSetupLocationServices() }
        needle?.setOnClickListener(clickListener)
        rose?.setOnClickListener(clickListener)
    }

    private fun setupCompassToLocationProvider() {
        compassLocationProvider = CompassToLocationProvider(context)
        compassLocationProvider?.setCompassToLocationListener(this)
    }

    private fun setupCoordinateLayouts() {
        if (Utils.isCompassSensorPresent(context)) {
            latitude?.setOnEditorActionListener(CustomEditTextActionEditor(context, longitude))
            longitude?.setOnEditorActionListener(CustomEditTextActionEditor(context, latitude))
            latitude?.addTextChangedListener(CustomEditTextWatcher(true, this))
            longitude?.addTextChangedListener(CustomEditTextWatcher(false, this))
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
            title?.setText(R.string.point_north_title)
        }
        title?.setText(R.string.empty)
    }

    private fun clearCoordinateInputOutOfRangeError(latitude: Boolean) {
        if (latitude) {
            latitudeLayout?.error = null
            latitudeLayout?.isErrorEnabled = false
        } else {
            longitudeLayout?.error = null
            longitudeLayout?.isErrorEnabled = false
        }
    }

    private fun onEmptyOrWrongInput(latitude: Boolean, outOfRange: Boolean) {
        setTitle(R.string.point_north_title, Color.BLACK)
        if (outOfRange) {
            setCoordinateInputOutOfRangeError(latitude)
        }
    }

    private fun setCoordinateInputOutOfRangeError(latitude: Boolean) {
        if (latitude) {
            latitudeLayout?.error = getString(R.string.error_latitude_out_of_range)
        } else {
            longitudeLayout?.error = getString(R.string.error_longitude_out_of_range)
        }
    }

    private fun checkUpAndSetupLocationServices() {
        if (Utils.isCompassSensorPresent(activity)) {
            setupLayoutOnLocationServicesCheckUp()
        }
    }

}