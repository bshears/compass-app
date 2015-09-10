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
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mariuszbaleczny.compass.Custom.CompassView;
import mariuszbaleczny.compass.Custom.CoordinateTextWatcher;
import mariuszbaleczny.compass.Custom.CustomEditText;
import mariuszbaleczny.compass.Custom.CustomEditTextActionEditor;

public class CompassFragment extends Fragment implements CompassToLocationProvider.CompassToLocationListener,
        CoordinateTextWatcher.OnCoordinateChangeListener {

    public static final String FRAGMENT_TAG = "CompassFragment";
    private final static String LOCATION_PROVIDER = "LocationProvider";
    private static final int REQUEST_CODE_SETTINGS = 0;

    private CompassView compassView;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private TextInputLayout latitudeTextInputLayout;
    private TextInputLayout longitudeTextInputLayout;
    private CustomEditText latitudeEditText;
    private CustomEditText longitudeEditText;

    private CompassToLocationProvider compassToLocationProvider;
    private Location targetLocation;

    private View.OnClickListener subtitleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setupLayoutOnLocationServicesCheckUp();
        }
    };

    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);

        setupCompassAndTextView(view);
        setCoordinatesEditTextEnabled(false);
        compassSensorPresenceTestAndSetup();
        setupCoordinatesInputField(view);

        return view;
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
        compassToLocationProvider.stopIfStarted();
    }

    private void setupCompassAndTextView(View v) {
        ImageView compassNeedleView = (ImageView) v.findViewById(R.id.fragment_compass_needle);
        ImageView compassRoseView = (ImageView) v.findViewById(R.id.fragment_compass_rose);
        compassView = new CompassView(compassRoseView, compassNeedleView);

        titleTextView = (TextView) v.findViewById(R.id.fragment_compass_title_text_view);
        subtitleTextView = (TextView) v.findViewById(R.id.fragment_compass_subtitle_text_view);
    }

    private void setCoordinatesEditTextEnabled(boolean value) {
        latitudeEditText.setEnabled(value);
        longitudeEditText.setEnabled(value);
    }

    private void compassSensorPresenceTestAndSetup() {
        if (!Utils.isCompassSensorPresent(getActivity())) {
            setTitleTextViewTo(getString(R.string.compass_not_detected_title), Color.RED);
            setSubtitleTextView("", null);
            setCoordinatesEditTextEnabled(false);
        } else {
            compassToLocationProvider = new CompassToLocationProvider(getActivity());
            compassToLocationProvider.setCompassToLocationListener(this);
            targetLocation = new Location(LOCATION_PROVIDER);
        }
    }

    private void setupCoordinatesInputField(View v) {
        latitudeTextInputLayout = (TextInputLayout) v.findViewById(R.id.fragment_compass_latitude_text_input);
        longitudeTextInputLayout = (TextInputLayout) v.findViewById(R.id.fragment_compass_longitude_text_input);
        latitudeEditText = (CustomEditText) v.findViewById(R.id.fragment_compass_latitude_edit_text);
        longitudeEditText = (CustomEditText) v.findViewById(R.id.fragment_compass_longitude_edit_text);

        if (compassToLocationProvider != null) {
            CustomEditTextActionEditor latitudeEditTextActionEditor = new CustomEditTextActionEditor(
                    getActivity(), latitudeEditText, longitudeEditText, compassToLocationProvider, true);
            CustomEditTextActionEditor longitudeEditTextActionEditor = new CustomEditTextActionEditor(
                    getActivity(), latitudeEditText, longitudeEditText, compassToLocationProvider, false);

            latitudeEditText.setOnEditorActionListener(latitudeEditTextActionEditor);
            longitudeEditText.setOnEditorActionListener(longitudeEditTextActionEditor);
            latitudeEditText.addTextChangedListener(new CoordinateTextWatcher(true, this));
            longitudeEditText.addTextChangedListener(new CoordinateTextWatcher(false, this));
        }
    }

    @Override
    public void onCompassPointerRotate(int roseAngle, int needleAngle) {
        compassView.rotateRose(roseAngle);
        compassView.rotateNeedle(needleAngle);
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
    public void setLayoutElementsOnProvider(boolean enabled) {
        setCoordinatesEditTextEnabled(enabled);
        if (enabled) {
            setSubtitleTextView(getString(R.string.info_text_subtitle), null);
        } else {
            setSubtitleTextView(getString(R.string.touch_info_error_subtitle), subtitleOnClickListener);
        }
    }

    private void buildAndShowLocationServicesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

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

    private boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(FRAGMENT_TAG, e.getMessage());
            return false;
        }
    }

    private void setTitleTextViewTo(String text, int color) {
        titleTextView.setText(text);
        titleTextView.setTextColor(color);
    }

    private void setSubtitleTextView(String text, View.OnClickListener onClickListener) {
        subtitleTextView.setText(text);
        subtitleTextView.setOnClickListener(onClickListener);
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
