package mariuszbaleczny.compass;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import mariuszbaleczny.compass.custom.CustomEditText;
import mariuszbaleczny.compass.custom.CustomEditTextActionEditor;
import mariuszbaleczny.compass.custom.CustomEditTextWatcher;
import mariuszbaleczny.compass.location.CompassToLocationProvider;
import mariuszbaleczny.compass.location.LocationHelper;

public class CompassFragment extends Fragment implements CompassToLocationProvider.CompassToLocationListener,
        CustomEditTextWatcher.OnCoordinateChangeListener {

    public static final String FRAGMENT_TAG = "CompassFragment";
    private static final String LOCATION_PROVIDER = "LocationProvider";
    private static final int REQUEST_CODE_SETTINGS = 0;

    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    private LocationHelper customTargetLocation;
    private Compass compass;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private TextInputLayout latitudeTextInputLayout;
    private TextInputLayout longitudeTextInputLayout;
    private CustomEditText latitudeEditText;
    private CustomEditText longitudeEditText;

    private CompassToLocationProvider compassToLocationProvider;

    private View.OnClickListener subtitleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setupLayoutOnLocationServicesCheckUp();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTargetLocation = new LocationHelper(new Location(LOCATION_PROVIDER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        setupLayoutElements(view);
        setupOnCompassSensorPresenceTest();
        setupCoordinatesInputField();
        setCoordinatesEditTextEnabled(false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isCompassSensorPresent(getActivity())) {
            setupLayoutOnLocationServicesCheckUp();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (compassToLocationProvider != null) {
            compassToLocationProvider.stopIfStarted();
        }
    }

    private void setupLayoutElements(View v) {
        ImageView compassNeedleView = (ImageView) v.findViewById(R.id.fragment_compass_needle);
        ImageView compassRoseView = (ImageView) v.findViewById(R.id.fragment_compass_rose);
        compass = new Compass(compassRoseView, compassNeedleView);

        titleTextView = (TextView) v.findViewById(R.id.fragment_compass_title_text_view);
        subtitleTextView = (TextView) v.findViewById(R.id.fragment_compass_subtitle_text_view);

        latitudeEditText = (CustomEditText) v.findViewById(R.id.fragment_compass_latitude_edit_text);
        longitudeEditText = (CustomEditText) v.findViewById(R.id.fragment_compass_longitude_edit_text);
        latitudeTextInputLayout = (TextInputLayout) v.findViewById(R.id.fragment_compass_latitude_text_input);
        longitudeTextInputLayout = (TextInputLayout) v.findViewById(R.id.fragment_compass_longitude_text_input);
    }

    private void setupOnCompassSensorPresenceTest() {
        if (!Utils.isCompassSensorPresent(getActivity())) {
            setTitleTextView(getString(R.string.compass_not_detected_title), Color.RED);
            setSubtitleTextView("", null);
            setCoordinatesEditTextEnabled(false);
        } else {
            compassToLocationProvider = new CompassToLocationProvider(getActivity());
            compassToLocationProvider.setCompassToLocationListener(this);
        }
    }

    private void setupCoordinatesInputField() {
        if (compassToLocationProvider != null) {
            CustomEditTextActionEditor latitudeActionEditor = new CustomEditTextActionEditor(
                    getActivity(), longitudeEditText);
            CustomEditTextActionEditor longitudeActionEditor = new CustomEditTextActionEditor(
                    getActivity(), latitudeEditText);

            latitudeEditText.setOnEditorActionListener(latitudeActionEditor);
            longitudeEditText.setOnEditorActionListener(longitudeActionEditor);
            latitudeEditText.addTextChangedListener(new CustomEditTextWatcher(true, this));
            longitudeEditText.addTextChangedListener(new CustomEditTextWatcher(false, this));
        }
    }

    private void setupLayoutOnLocationServicesCheckUp() {
        if (Utils.isLocationServicesEnabled(getActivity())) {
            setTitleTextView(getString(R.string.point_north_title), Color.BLACK);
            compassToLocationProvider.startIfNotStarted();
            setLayoutElementsOnProvider(true);
        } else {
            setTitleTextView("", Color.BLACK);
            compassToLocationProvider.stopIfStarted();
            setLayoutElementsOnProvider(false);
            buildAndShowLocationServicesDialog();
        }
    }

    @Override
    public void onCompassPointerRotate(int roseAngle, int needleAngle) {
        compass.rotateRose(roseAngle);
        compass.rotateNeedle(needleAngle);
    }

    /**
     *  Method handles actions respectively for input coordinate entered in EditText.
     *  When coordinate argument is Double.NaN it means EditText field is empty, so
     *  error will be disabled and target location set to null (reset).
     *  Proper coordinate argument will be set to customTargetLocation and its Location
     *  will be passed to compassToLocationProvider. Passing a null will cause resetting
     *  targetLocation, which is intended.
     *  Sets TitleTextView and coordinates EditTexts errors status.
     *  @param latitude   indicates whether coordinate is latitude or longitude coordinate
     *  @param coordinate represents value of coordinate in Double
     */
    @Override
    public void onCoordinateChanged(boolean latitude, Double coordinate) {
        // when coordinate is NaN (i.e. when empty EditText)
        if (Double.isNaN(coordinate)) {
            clearCoordinateInputOutOfRangeError(latitude);
            setTitleTextView(getString(R.string.point_north_title), Color.BLACK);
            // set null location, so needle will reset its state
            compassToLocationProvider.setTargetLocation(null);
            return;
        }

        if (latitude) {
            customTargetLocation.setLatitude(coordinate);
        } else {
            customTargetLocation.setLongitude(coordinate);
        }
        // even when coordinate is out its of range, then target location will be set to null (reset)
        compassToLocationProvider.setTargetLocation(customTargetLocation.getLocation());

        if (customTargetLocation.isCorrect()) {
            setTitleTextView(getString(R.string.point_location_title), Color.BLACK);
            clearCoordinateInputOutOfRangeError(latitude);
        }
        // correct location doesn't mean that input value is incorrect!
        if (!Utils.isCoordinateInRange(coordinate, latitude)) {
            onEmptyOrWrongInput(latitude, true);
        }
    }

    @Override
    public void setLayoutElementsOnProvider(boolean enabled) {
        setCoordinatesEditTextEnabled(enabled);
        if (enabled) {
            setSubtitleTextView(getString(R.string.info_text_subtitle), null);
            if (customTargetLocation.isCorrect()) {
                compassToLocationProvider.setTargetLocation(customTargetLocation.getLocation());
                setTitleTextView(getString(R.string.point_location_title), Color.BLACK);
            }
        } else {
            setTitleTextView(getString(R.string.point_north_title), Color.BLACK);
            setSubtitleTextView(getString(R.string.touch_info_error_subtitle), subtitleOnClickListener);
        }
    }

    private void buildAndShowLocationServicesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
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

    private void setCoordinatesEditTextEnabled(boolean value) {
        latitudeEditText.setEnabled(value);
        longitudeEditText.setEnabled(value);
    }

    private void setTitleTextView(String text, int color) {
        titleTextView.setText(text);
        titleTextView.setTextColor(color);
    }

    private void setSubtitleTextView(String text, View.OnClickListener onClickListener) {
        subtitleTextView.setText(text);
        subtitleTextView.setOnClickListener(onClickListener);
    }

    private void onEmptyOrWrongInput(boolean latitude, boolean outOfRange) {
        setTitleTextView(getString(R.string.point_north_title), Color.BLACK);
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
            latitudeTextInputLayout.setErrorEnabled(false);
        } else {
            longitudeTextInputLayout.setError(null);
            longitudeTextInputLayout.setErrorEnabled(false);
        }
    }
}
