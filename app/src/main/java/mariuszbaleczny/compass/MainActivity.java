package mariuszbaleczny.compass;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
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

public class MainActivity extends Activity implements CompassToLocationProvider.ChangeEventListener {

    private static final int NUMBER_OF_MEASUREMENTS_FOR_SMOOTHING_DATA = 5;
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
                isCompassPresentInDevice();
                titleTextView.setText(getString(R.string.point_north));
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
                isCompassPresentInDevice();
                titleTextView.setText(getString(R.string.point_north));
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
        titleTextView = (TextView) findViewById(R.id.pointTitleTextView);
        subtitleTextView = (TextView) findViewById(R.id.pointLocationAddressTextView);
        subtitleTextView.setVisibility(View.INVISIBLE);

        latitudeEditText.addTextChangedListener(latitudeTextWatcher);
        longitudeEditText.addTextChangedListener(longitudeTextWatcher);

        latitudeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    latitudeEditText.clearFocus();
                    if (longitudeEditText.getText().length() != 0) {
                        hideKeyboard();
                    } else {
                        longitudeEditText.requestFocus();
                    }
                    if (v.getText().length() == 0) {
                        compassToLocationProvider.resetTargetLocation();
                    }
                }
                return false;
            }
        });

        longitudeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    longitudeEditText.clearFocus();
                    if (latitudeEditText.getText().length() != 0) {
                        hideKeyboard();
                    } else {
                        latitudeEditText.requestFocus();
                    }
                    if (v.getText().length() == 0) {
                        compassToLocationProvider.resetTargetLocation();
                    }
                }
                return false;
            }
        });

        setLayoutElements(false);

        compassToLocationProvider = new CompassToLocationProvider(this,
                NUMBER_OF_MEASUREMENTS_FOR_SMOOTHING_DATA);
        compassToLocationProvider.setChangeEventListener(this);
    }

    @Override
    public void onCompassToLocationChange(double azimuth) {
        animatePointer(azimuth);
        currentAngle = -azimuth;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (isCompassPresentInDevice() && requestCode == REQUEST_CODE_SETTINGS) {
            setLayoutElements(true);
        } else {
            setLayoutElements(false);
        }
    }

    @Override
    public void onLocationChange(Address address) {
        setTitleTextViewTo(getString(R.string.point_location), Color.BLACK);
        String addressString = "";

        try {
            if (address.getMaxAddressLineIndex() > 0) {
                addressString += address.getAddressLine(0);
                for (int i = 1; i < address.getMaxAddressLineIndex(); i++) {
                    addressString += ", " + address.getAddressLine(i);
                }
            } else {
                if (address.getCountryCode().equals("")) {
                    addressString = address.getLocality();
                } else {
                    addressString = address.getLocality() + ", " + address.getCountryCode();
                }
            }

            subtitleTextView.setText(addressString);
            subtitleTextView.setVisibility(View.VISIBLE);
            subtitleTextView.setOnClickListener(null);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showInfoToastFromMainActivity(String text, int length) {
        showInfoToastWith(text, length);
    }

    @Override
    public void recheckLocationAndNetworkServicesSettings() {
        isCompassPresentInDevice();
    }

    protected void onResume() {
        super.onResume();
        isCompassPresentInDevice();
    }

    protected void onPause() {
        super.onPause();
        compassToLocationProvider.stop();
    }

    public void showInfoToastWith(String text, int length) {
        if (infoToast == null || infoToast.getView().getWindowVisibility() != View.VISIBLE) {
            infoToast = Toast.makeText(this, text, length);
            infoToast.show();
        }
    }

    private void setLayoutElements(boolean value) {
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

    private boolean isLatitudeInRange(double latitude){
        targetLatitudeInRange = (latitude > -90.0d && latitude < 90.0d);
        return targetLatitudeInRange;
    }

    private boolean isLongitudeInRange(double longitude){
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
        return (s.length() == 0 || s.toString().equals("-") || s.toString().equals(""));
    }

    private boolean isCompassSensorPresent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean isCompassPresentInDevice() {
        if (isCompassSensorPresent()) {
            setTitleTextViewTo(getString(R.string.point_north), Color.BLACK);
            compassToLocationProvider.startIfNotStarted();

            return isLocationAndNetworkEnabled();
        } else {
            setTitleTextViewTo(getString(R.string.compass_not_detected), Color.RED);
            setLayoutElements(false);

            return false;
        }
    }

    private boolean isLocationAndNetworkEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gpsEnabled || !isOnline()) {
            setSubtitleTextViewTo(getString(R.string.touch_info_error));
            subtitleTextView.setVisibility(View.VISIBLE);
            subtitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isLocationAndNetworkEnabled();
                }
            });

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            if (!gpsEnabled && isOnline()) {
                dialog.setTitle(getString(R.string.location_services_off));
            } else if (!isOnline() && gpsEnabled) {
                dialog.setTitle(getString(R.string.no_internet_connection));
            } else {
                dialog.setTitle(getString(R.string.no_internet_no_locations));
            }

            dialog.setMessage(getString(R.string.go_to_settings));

            dialog.setPositiveButton(getString(R.string.agree), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
                    startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
                }
            });

            dialog.setNegativeButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                }
            });
            dialog.show();

            return false;
        } else {
            compassToLocationProvider.startIfNotStarted();
            subtitleTextView.setVisibility(View.VISIBLE);
            subtitleTextView.setText(getString(R.string.info_text));
            subtitleTextView.setOnClickListener(null);
            setLayoutElements(true);

            return true;
        }
    }
}