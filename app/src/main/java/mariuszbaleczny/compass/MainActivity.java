package mariuszbaleczny.compass;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements CompassToLocationProvider.CompassToLocationListener {

    private static final int REQUEST_CODE_SETTINGS = 0;

    private CompassToLocationProvider compassToLocationProvider;

    private double currentAzimuth = 0d;
    private double currentNorthAngle = 0d;
    private double targetLatitude = Double.NaN;
    private double targetLongitude = Double.NaN;
    private boolean targetLatitudeInRange = false;
    private boolean targetLongitudeInRange = false;

    private ImageView compassPointerView;
    private ImageView compassRoseView;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private CustomEditText latitudeEditText;
    private CustomEditText longitudeEditText;

    private View.OnClickListener subtitleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setupLayoutAndCompassProvider();
        }
    };

    private TextView.OnEditorActionListener latitudeEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                controlFocus(longitudeEditText);
                resetTargetLocationIfEmptyTextView(v);
                return true;
            } else {
                return false;
            }
        }
    };
    private TextView.OnEditorActionListener longitudeEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                controlFocus(latitudeEditText);
                resetTargetLocationIfEmptyTextView(v);
                return true;
            } else {
                return false;
            }

        }
    };

    private void controlFocus(CustomEditText editText) {
        if (editText.getText().length() != 0) {
            hideKeyboard();
        } else {
            editText.requestFocus();
        }
    }

    private void resetTargetLocationIfEmptyTextView(TextView v) {
        if (v.getText().length() == 0) {
            compassToLocationProvider.resetTargetLocation();
        }
    }

    private TextWatcher latitudeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isEmptyOrDashOrDot(s)) {
                targetLatitude = Double.parseDouble(s.toString());
                if (!isLatitudeInRange(targetLatitude)) {
                    targetLatitude = Double.NaN;
                }
            } else {
                targetLatitudeInRange = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (targetLatitudeInRange & targetLongitudeInRange) {
                compassToLocationProvider.setTargetLocationCoordinates(targetLatitude, targetLongitude);
            } else {
                compassToLocationProvider.resetTargetLocation();
                setupLayoutAndCompassProvider();
            }
        }
    };
    private TextWatcher longitudeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isEmptyOrDashOrDot(s)) {
                targetLongitude = Double.parseDouble(s.toString());
                if (!isLongitudeInRange(targetLongitude)) {
                    targetLongitude = Double.NaN;
                }
            } else {
                targetLongitudeInRange = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (targetLatitudeInRange && targetLongitudeInRange) {
                compassToLocationProvider.setTargetLocationCoordinates(targetLatitude, targetLongitude);
            } else {
                compassToLocationProvider.resetTargetLocation();
                setupLayoutAndCompassProvider();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassPointerView = (ImageView) findViewById(R.id.compass_pointer);
        compassRoseView = (ImageView) findViewById(R.id.compass_rose);
        latitudeEditText = (CustomEditText) findViewById(R.id.latitudeMainActivity);
        longitudeEditText = (CustomEditText) findViewById(R.id.longitudeMainActivity);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        subtitleTextView = (TextView) findViewById(R.id.subtitleTextView);

        latitudeEditText.addTextChangedListener(latitudeTextWatcher);
        longitudeEditText.addTextChangedListener(longitudeTextWatcher);
        latitudeEditText.setOnEditorActionListener(latitudeEditorActionListener);
        longitudeEditText.setOnEditorActionListener(longitudeEditorActionListener);

        setCoordinatesEditTextEnabled(false);

        compassToLocationProvider = new CompassToLocationProvider(this);
        compassToLocationProvider.setCompassToLocationListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCompassSensorPresent()) {
            setTitleTextViewTo(getString(R.string.compass_not_detected_title), Color.RED);
            setCoordinatesEditTextEnabled(false);
            setSubtitleTextView("", null);
        } else {
            setupLayoutAndCompassProvider();
        }
    }

    private boolean isCompassSensorPresent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    private void setupLayoutAndCompassProvider() {
        if (isLocationServiceEnabled()) {
            setTitleTextViewTo(getString(R.string.point_north_title), Color.BLACK);
            setCoordinatesEditTextEnabled(true);
            setLayoutElementsOnProvider(true);
        } else {
            setLayoutElementsOnProvider(false);
            setCoordinatesEditTextEnabled(false);
            buildAndShowLocationServicesDialog();
        }
        compassToLocationProvider.startIfNotStarted();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassToLocationProvider.stopIfStarted();
    }

    @Override
    public void onCompassPointerRotate(int north, int azimuth) {
        animatePointer(azimuth);
        animateRose(north);
        currentAzimuth = -azimuth;
        currentNorthAngle = -north;
    }

    @Override
    public void setTitleOnPointingLocation() {
        setTitleTextViewTo(getString(R.string.point_location_title), Color.BLACK);
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

    private boolean isLatitudeInRange(double latitude) {
        targetLatitudeInRange = (latitude > -90.0d && latitude < 90.0d);
        return targetLatitudeInRange;
    }

    private boolean isLongitudeInRange(double longitude) {
        targetLongitudeInRange = (longitude > -180.0d && longitude < 180.0d);
        return targetLongitudeInRange;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void animatePointer(int angle) {
        RotateAnimation ra = new RotateAnimation(
                (float) currentAzimuth,
                (float) -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(600);
        ra.setFillAfter(true);

        compassPointerView.startAnimation(ra);
    }

    private void animateRose(int angle) {
        RotateAnimation ra = new RotateAnimation(
                (float) currentNorthAngle,
                (float) -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(600);
        ra.setFillAfter(true);

        compassRoseView.startAnimation(ra);
    }

    private boolean isEmptyOrDashOrDot(CharSequence s) {
        return (s.length() == 0 || s.toString().equals(getString(R.string.minus_sign))
                || s.toString().equals("") || s.toString().equals(getString(R.string.dot_sign)));
    }

    private boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
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
}