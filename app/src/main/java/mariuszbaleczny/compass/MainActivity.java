package mariuszbaleczny.compass;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import mariuszbaleczny.compass.Custom.CompassView;
import mariuszbaleczny.compass.Custom.CoordinateTextWatcher;
import mariuszbaleczny.compass.Custom.CustomEditText;
import mariuszbaleczny.compass.Custom.CustomEditTextActionEditor;

public class MainActivity extends AppCompatActivity implements CompassToLocationProvider.CompassToLocationListener,
        CoordinateTextWatcher.OnCoordinateChangeListener {

    private final static String LOCATION_PROVIDER = "LocationProvider";
    private static final int REQUEST_CODE_SETTINGS = 0;

    private CompassToLocationProvider compassToLocationProvider;
    private Location targetLocation;

    private CompassView compassView;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private TextInputLayout latitudeTextInputLayout;
    private TextInputLayout longitudeTextInputLayout;
    private CustomEditText latitudeEditText;
    private CustomEditText longitudeEditText;

    private View.OnClickListener subtitleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setupLayoutOnLocationServicesCheckUp();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        setupCompassAndTextView();
        setCoordinatesEditTextEnabled(false);
        compassSensorPresenceTestAndSetup();
        setupCoordinatesInputField();
    }

    private void compassSensorPresenceTestAndSetup() {
        if (!Utils.isCompassSensorPresent(this)) {
            setTitleTextViewTo(getString(R.string.compass_not_detected_title), Color.RED);
            setSubtitleTextView("", null);
            setCoordinatesEditTextEnabled(false);
        } else {
            compassToLocationProvider = new CompassToLocationProvider(this);
            compassToLocationProvider.setCompassToLocationListener(this);
            targetLocation = new Location(LOCATION_PROVIDER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isCompassSensorPresent(this)) {
            setupLayoutOnLocationServicesCheckUp();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassToLocationProvider.stopIfStarted();
    }

    private void setupCompassAndTextView() {
        ImageView compassNeedleView = (ImageView) findViewById(R.id.compass_needle);
        ImageView compassRoseView = (ImageView) findViewById(R.id.compass_rose);
        compassView = new CompassView(compassRoseView, compassNeedleView);

        titleTextView = (TextView) findViewById(R.id.title_text_view);
        subtitleTextView = (TextView) findViewById(R.id.subtitle_text_view);
    }

    private void setupCoordinatesInputField() {
        latitudeTextInputLayout = (TextInputLayout) findViewById(R.id.latitude_text_input);
        longitudeTextInputLayout = (TextInputLayout) findViewById(R.id.longitude_text_input);
        latitudeEditText = (CustomEditText) findViewById(R.id.latitude_edit_text);
        longitudeEditText = (CustomEditText) findViewById(R.id.longitude_edit_text);

        if (compassToLocationProvider != null) {
            CustomEditTextActionEditor latitudeEditTextActionEditor = new CustomEditTextActionEditor(
                    this, latitudeEditText, longitudeEditText, compassToLocationProvider, true);
            CustomEditTextActionEditor longitudeEditTextActionEditor = new CustomEditTextActionEditor(
                    this, latitudeEditText, longitudeEditText, compassToLocationProvider, false);

            latitudeEditText.setOnEditorActionListener(latitudeEditTextActionEditor);
            longitudeEditText.setOnEditorActionListener(longitudeEditTextActionEditor);
            latitudeEditText.addTextChangedListener(new CoordinateTextWatcher(true, this));
            longitudeEditText.addTextChangedListener(new CoordinateTextWatcher(false, this));
        }
    }

    private void setupLayoutOnLocationServicesCheckUp() {
        if (isLocationServiceEnabled()) {
            setLayoutElementsOnProvider(true);
            setTitleTextViewTo(getString(R.string.point_north_title), Color.BLACK);
        } else {
            setLayoutElementsOnProvider(false);
            buildAndShowLocationServicesDialog();
        }
        compassToLocationProvider.startIfNotStarted();
    }

    @Override
    public void onCompassPointerRotate(int roseAngle, int needleAngle) {
        compassView.rotateRose(roseAngle);
        compassView.rotateNeedle(needleAngle);
    }

    @Override
    public void setLayoutElementsOnProvider(boolean enabled) {
        setCoordinatesEditTextEnabled(enabled);
        if (enabled) {
            setSubtitleTextView(getString(R.string.info_text_subtitle), null);
        } else {
            setSubtitleTextView(getString(R.string.touch_info_error_subtitle), subtitleOnClickListener);
        }
    }

    private void setSubtitleTextView(String text, View.OnClickListener onClickListener) {
        subtitleTextView.setText(text);
        subtitleTextView.setOnClickListener(onClickListener);
    }

    private void setCoordinatesEditTextEnabled(boolean value) {
        latitudeEditText.setEnabled(value);
        longitudeEditText.setEnabled(value);
    }

    private void setTitleTextViewTo(String text, int color) {
        titleTextView.setText(text);
        titleTextView.setTextColor(color);
    }

    private boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
            return false;
        }
    }

    private void buildAndShowLocationServicesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(getString(R.string.title_alert_dialog));
        dialog.setMessage(getString(R.string.message_alert_dialog));
        dialog.setPositiveButton(getString(R.string.positive_alert_dialog),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
                    }
                });
        dialog.setNegativeButton(getString(R.string.negative_alert_dialog),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });

        dialog.show();
    }

    @Override
    public void onCoordinateChanged(boolean latitude, double coordinate) {
        if (latitude) {
            targetLocation.setLatitude(coordinate);
        } else {
            targetLocation.setLongitude(coordinate);
        }
        setTitleTextViewTo(getString(R.string.point_location_title), Color.BLACK);
        compassToLocationProvider.setTargetLocation(targetLocation);
        clearCoordinateInputOutOfRangeError(latitude);
    }

    @Override
    public void onEmptyOrWrongInput(boolean latitude, boolean outOfRange) {
        compassToLocationProvider.resetTargetLocation();
        setTitleTextViewTo(getString(R.string.point_north_title), Color.BLACK);
        if (outOfRange) {
            setCoordinateInputOutOfRangeError(latitude);
        }
    }

    private void setCoordinateInputOutOfRangeError(boolean latitude) {
        if (latitude) {
            latitudeTextInputLayout.setError(getString(R.string.error_latitude_out_of_range));
        } else {
            longitudeTextInputLayout.setError(getString(R.string.error_longitude_out_of_range));
        }
    }

    private void clearCoordinateInputOutOfRangeError(boolean latitude) {
        if (latitude) {
            latitudeTextInputLayout.setError(null);
        } else {
            longitudeTextInputLayout.setError(null);
        }
    }
}