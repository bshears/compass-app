package mariuszbaleczny.compass.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog.Builder
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.pawegio.kandroid.d
import com.pawegio.kandroid.find
import com.pawegio.kandroid.textWatcher
import mariuszbaleczny.compass.R
import mariuszbaleczny.compass.R.layout
import mariuszbaleczny.compass.R.string
import mariuszbaleczny.compass.R.style
import mariuszbaleczny.compass.Utils
import mariuszbaleczny.compass.loader.CompassPresenterLoader
import mariuszbaleczny.compass.location.CompassLocationPointer
import mariuszbaleczny.compass.mvp.CompassMvp
import mariuszbaleczny.compass.mvp.CompassMvp.Presenter
import mariuszbaleczny.compass.mvp.CompassPresenter
import mariuszbaleczny.compass.ui.activity.CompassActivity
import mariuszbaleczny.compass.ui.custom.CustomEditText
import mariuszbaleczny.compass.ui.helper.CompassRotateHelper

/**
 * Created by mariusz on 03.11.16.
 */
class CompassFragment : Fragment(), CompassMvp.View {
    companion object {
        const val COMPASS_APPLICATION: String = "compass_location_provider"
        const val REQUEST_CODE_SETTINGS: Int = 0
        const val COMPASS_LOADER_ID: Int = 100
        val TAG: String = "CompassFragment"
    }

    private var presenter: Presenter? = null
    private var compassPointer: CompassLocationPointer? = null

    private var compassView: CompassRotateHelper? = null
    private var title: TextView? = null
    private var subtitle: TextView? = null
    private var latitude: CustomEditText? = null
    private var longitude: CustomEditText? = null
    private var latitudeLayout: TextInputLayout? = null
    private var longitudeLayout: TextInputLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loaderManager.initLoader(COMPASS_LOADER_ID, savedInstanceState, object : LoaderCallbacks<Presenter> {
            override fun onLoadFinished(loader: Loader<Presenter>?, data: Presenter?) {
                onPresenterLoad(data!! as CompassPresenter)
            }

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Presenter> {
                return CompassPresenterLoader(context)
            }

            override fun onLoaderReset(loader: Loader<Presenter>?) {
                presenter = null
            }

        })
    }

    override fun onPresenterLoad(presenter: CompassPresenter) {
        this.presenter = presenter
        this.presenter?.bindView(this)
        if (compassPointer == null) compassPointer = CompassLocationPointer(context)
        this.presenter?.setCompassPointer(compassPointer!!)
        compassPointer?.startIfNotStarted()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View? = inflater?.inflate(layout.fragment_compass, container, false)
        setupLayoutWidgets(v)
        setupTitleAndSubtitle()
        setupCompassView(v)
        setupCoordinateLayouts()
        setCoordinateInputDisabled()
        return v
    }

    override fun asFragment(): Fragment {
        return this
    }

    override fun onResume() {
        super.onResume()
        setupLayoutOnLocationServicesCheckUp()
    }

    override fun onPause() {
        super.onPause()
        compassPointer?.stopIfStarted()
    }

    override fun setCoordinateInputEnabled() {
        latitude?.isEnabled = true
        longitude?.isEnabled = true
    }

    override fun setCoordinateInputDisabled() {
        latitude?.isEnabled = false
        longitude?.isEnabled = false
    }

    override fun setSubtitle(@StringRes resId: Int) {
        subtitle?.setText(resId)
    }

    override fun setTitle(resId: Int) {
        title?.setText(resId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true
    }

    override fun rotateCompass(roseAngle: Int, needleAngle: Int) {
        compassView?.rotateRose(roseAngle)
        compassView?.rotateNeedle(needleAngle)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CompassLocationPointer.LOC_PERMISSION_ON_START_REQUEST_CODE -> {
                if (Utils.areGranted(grantResults)) {
                    compassPointer?.startIfNotStarted()
                } else {
                    setCoordinateInputDisabled()
                }
            }
        }
    }

    override fun onLatitudeInRange() {
        latitudeLayout?.error = null
        latitudeLayout?.isErrorEnabled = false
    }

    override fun onLatitudeOutOfRange() {
        latitudeLayout?.error = getString(string.error_latitude_out_of_range)
    }

    override fun onLongitudeInRange() {
        longitudeLayout?.error = null
        longitudeLayout?.isErrorEnabled = false
    }

    override fun onLongitudeOutOfRange() {
        longitudeLayout?.error = getString(string.error_longitude_out_of_range)
    }

    override fun onNullLatitude() {
        latitudeLayout?.error = null
        latitudeLayout?.isErrorEnabled = false
        setTitle(string.needle_free_mode)
    }

    override fun onNullLongitude() {
        latitudeLayout?.error = null
        latitudeLayout?.isErrorEnabled = false
        setTitle(string.needle_free_mode)
    }

    private fun setupLayoutOnLocationServicesCheckUp() {
        if (Utils.isLocationServicesEnabled(context)) {
            setTitle(string.needle_free_mode)
            compassPointer?.startIfNotStarted()
        } else {
            setTitle(string.empty)
            setSubtitle(string.touch_info_error_subtitle,
                    OnClickListener { setupLayoutOnLocationServicesCheckUp() })
            compassPointer?.stopIfStarted()
            setCoordinateInputDisabled()
            buildAndShowLocationServicesDialog()
        }
    }

    private fun buildAndShowLocationServicesDialog() {
        val builder: Builder = Builder(context, style.AlertDialogTheme)
        builder.setTitle(string.title_alert_dialog)
                .setMessage(string.message_alert_dialog)
                .setPositiveButton(string.positive_alert_dialog,
                        { dialogInterface, i ->
                            startActivityForResult(
                                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                    REQUEST_CODE_SETTINGS
                            )
                        })
                .setNegativeButton(string.negative_alert_dialog, null)
                .show()
    }

    private fun setSubtitle(resId: Int, listener: OnClickListener?) {
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
        if (!Utils.arePermissionsGranted(context, CompassLocationPointer.COMPASS_PERMISSIONS)) {
            ActivityCompat.requestPermissions(context as CompassActivity,
                    CompassLocationPointer.COMPASS_PERMISSIONS,
                    CompassLocationPointer.LOC_PERMISSION_ON_START_REQUEST_CODE)
        }
    }

    private fun setupCoordinateLayouts() {
        if (Utils.isCompassSensorPresent(context)) {
            latitude?.setOnEditorActionListener({ v, id, event -> onCoordinateFieldAction(longitude, v, id) })
            longitude?.setOnEditorActionListener({ v, id, event -> onCoordinateFieldAction(latitude, v, id) })
            latitude?.textWatcher { afterTextChanged { s -> presenter?.onLatitudeChanged(parseDouble(s)) } }
            longitude?.textWatcher { afterTextChanged { s -> presenter?.onLongitudeChanged(parseDouble(s)) } }
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

    private fun parseDouble(s: Editable?): Double? {
        try {
            return s.toString().toDouble()
        } catch (ex: NumberFormatException) {
            d(tag, ex.message as String)
            return null
        }
    }

    private fun setupTitleAndSubtitle() {
        if (!Utils.isCompassSensorPresent(context)) {
            title?.setText(string.compass_not_detected_title)
            subtitle?.setText(string.empty)
        } else {
            title?.setText(string.needle_free_mode)
            subtitle?.setText(string.info_text_subtitle)
        }
    }

}