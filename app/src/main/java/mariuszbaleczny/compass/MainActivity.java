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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CompassToLocationProvider.ChangeEventListener {

    private static final int REQUEST_CODE_SETTINGS = 0;

    private static Toast infoToast;
    private CompassToLocationProvider compassToLocationProvider;

    private double currentAngle = 0d;
    private double targetLatitude = Double.NaN;
    private double targetLongitude = Double.NaN;
    private boolean targetLatitudeInRange = false;
    private boolean targetLongitudeInRange = false;

    private ImageView compassPointerView;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private TextView.OnEditorActionListener latitudeEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (longitudeEditText.getText().length() != 0) {
                    hideKeyboard();
                } else {
                    longitudeEditText.requestFocus();
                }
                if (v.getText().length() == 0) {
                    compassToLocationProvider.resetTargetLocation();
                }
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
                if (latitudeEditText.getText().length() != 0) {
                    hideKeyboard();
                } else {
                    latitudeEditText.requestFocus();
                }
                if (v.getText().length() == 0) {
                    compassToLocationProvider.resetTargetLocation();
                }
                return true;
            } else {
                return false;
            }

        }
    };
    private View.OnClickListener subtitleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isLocationAndNetworkEnabled();
        }
    };
    private TextWatcher latitudeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isEmptyOrDash(s)) {
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
                checkSettingsAndSetCorrespondingTitle();
                titleTextView.setText(getString(R.string.point_north_title));
            }
        }
    };
    private TextWatcher longitudeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isEmptyOrDash(s)) {
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
                checkSettingsAndSetCorrespondingTitle();
                titleTextView.setText(getString(R.string.point_north_title));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassPointerView = (ImageView) findViewById(R.id.arrowPointer);
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
        compassToLocationProvider.setChangeEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSettingsAndSetCorrespondingTitle()) {
            setLayoutElementsOnProvider(true);
        } else {
            setLayoutElementsOnProvider(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassToLocationProvider.stopIfStarted();
    }

    @Override
    public void onCompassToLocationChange(double azimuth) {
        animatePointer(azimuth);
        currentAngle = -azimuth;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (checkSettingsAndSetCorrespondingTitle() && requestCode == REQUEST_CODE_SETTINGS) {
            setCoordinatesEditTextEnabled(true);
        } else {
            setCoordinatesEditTextEnabled(false);
        }
    }

    @Override
    public void onLocationPoint() {
        setTitleTextViewTo(getString(R.string.point_location_title), Color.BLACK);
    }

    @Override
    public void showInfoToastFromMainActivity(String text, int length) {
        showInfoToastWith(text, length);
    }

    @Override
    public void setLayoutElementsOnProvider(boolean enabled) {
        setCoordinatesEditTextEnabled(enabled);
        if (enabled) {
            setSubtitleTextViewTo(getString(R.string.info_text_subtitle));
            subtitleTextView.setOnClickListener(null);
        } else {
            setSubtitleTextViewTo(getString(R.string.touch_info_error_subtitle));
            subtitleTextView.setOnClickListener(subtitleOnClickListener);
        }
    }

    public void showInfoToastWith(String text, int toastLength) {
        if (infoToast == null || infoToast.getView().getWindowVisibility() != View.VISIBLE) {
            infoToast = Toast.makeText(this, text, toastLength);
            infoToast.show();
        }
    }

    private void setCoordinatesEditTextEnabled(boolean value) {
        latitudeEditText.setEnabled(value);
        longitudeEditText.setEnabled(value);
    }

    private void setTitleTextViewTo(String text, int color) {
        titleTextView.setText(text);
        titleTextView.setTextColor(color);
    }

    private void setSubtitleTextViewTo(String text) {
        subtitleTextView.setText(text);
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

    private void animatePointer(double angle) {
        RotateAnimation ra = new RotateAnimation(
                (float) currentAngle,
                (float) -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);

        compassPointerView.startAnimation(ra);
    }

    private boolean isEmptyOrDash(CharSequence s) {
        return (s.length() == 0 || s.toString().equals(getString(R.string.minus_sign)) || s.toString().equals(""));
    }

    public boolean checkSettingsAndSetCorrespondingTitle() {
        if (isCompassSensorPresent()) {
            setTitleTextViewTo(getString(R.string.point_north_title), Color.BLACK);
            compassToLocationProvider.startIfNotStarted();

            return isLocationAndNetworkEnabled();
        } else {
            setTitleTextViewTo(getString(R.string.compass_not_detected_title), Color.RED);
            setCoordinatesEditTextEnabled(false);

            return false;
        }
    }

    private boolean isCompassSensorPresent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    private boolean isLocationAndNetworkEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gpsEnabled) {
            setLayoutElementsOnProvider(false);

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(getString(R.string.title_alert_dialog));
            dialog.setMessage(getString(R.string.message_alert_dialog));
            dialog.setPositiveButton(getString(R.string.positive_alert_dialog), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
                }
            });
            dialog.setNegativeButton(getString(R.string.negative_alert_dialog), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                }
            });
            dialog.show();

            return false;
        } else {
            compassToLocationProvider.startIfNotStarted();

            return true;
        }
    }
}