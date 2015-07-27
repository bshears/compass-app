package mariuszbaleczny.compass;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CompassToLocationProvider.ChangeEventListener {

    private static final int NUMBER_OF_MEASUREMENTS_FOR_SMOOTHING_DATA = 3;
    private CompassToLocationProvider compassToLocationProvider;

    private double currentAngle = 0d;
    private double targetLatitude = Double.NaN;
    private double targetLongitude = Double.NaN;

    private boolean isTargetLatitude = false;
    private boolean isTargetLongitude = false;

    private ImageView compassPointerView;
    private TextView pointTitleTextView;
    private TextView pointLocationAddressTextView;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private Toast infoToast;

    private TextWatcher latitudeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isEmptyOrDash(s)) {
                targetLatitude = Double.parseDouble(s.toString());
                if (targetLatitude >= -90.0d && targetLatitude <= 90.0d) {
                    isTargetLatitude = true;
                } else {
                    targetLatitude = Double.NaN;
                    isTargetLatitude = false;
                }
            } else {
                isTargetLatitude = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isTargetLatitude & isTargetLongitude) {
                compassToLocationProvider.setTargetLocationCoordinates(targetLatitude, targetLongitude);
            } else {
                compassToLocationProvider.resetTargetLocation();
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
                if (targetLongitude >= -180.0d && targetLongitude <= 180.0d) {
                    isTargetLongitude = true;
                } else {
                    targetLongitude = Double.NaN;
                    isTargetLongitude = false;
                }
            } else {
                isTargetLongitude = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isTargetLatitude && isTargetLongitude) {
                compassToLocationProvider.setTargetLocationCoordinates(targetLatitude, targetLongitude);
            } else {
                compassToLocationProvider.resetTargetLocation();
                pointTitleTextView.setText(getString(R.string.point_north));
                pointLocationAddressTextView.setVisibility(View.INVISIBLE);
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
        pointTitleTextView = (TextView) findViewById(R.id.pointTitleTextView);
        pointLocationAddressTextView = (TextView) findViewById(R.id.pointLocationAddressTextView);
        pointLocationAddressTextView.setVisibility(View.INVISIBLE);

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
                }
                return false;
            }
        });

        longitudeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    longitudeEditText.clearFocus();
                    if(latitudeEditText.getText().length() != 0) {
                        hideKeyboard();
                    } else {
                        latitudeEditText.requestFocus();
                    }
                }
                return false;
            }
        });

        compassToLocationProvider = new CompassToLocationProvider(this,
                NUMBER_OF_MEASUREMENTS_FOR_SMOOTHING_DATA);
        compassToLocationProvider.setChangeEventListener(this);
    }

    protected void onResume() {
        super.onResume();
        if (isCompassSensorPresent()) {
            isLocationEnabled();
        } else {

            if (infoToast == null || infoToast.getView().getWindowVisibility() != View.VISIBLE) {
                infoToast = Toast.makeText(this, getString(R.string.compass_not_present),
                        Toast.LENGTH_SHORT);
                infoToast.show();
            }
        }
    }

    protected void onPause() {
        super.onPause();
        compassToLocationProvider.stop();
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
        return (s.length() == 0 || s.toString().equals("-"));
    }

    @Override
    public void onCompassToLocationChange(double azimuth) {
        animatePointer(azimuth);
        currentAngle = -azimuth;
    }

    private boolean isCompassSensorPresent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    private void isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getString(R.string.location_services_off));
            dialog.setPositiveButton(getString(R.string.go_to_location_services), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                }
            });
            dialog.show();
        } else {
            compassToLocationProvider.start();
        }
    }

    @Override
    public void onLocationStateChange(Address address) {
        pointTitleTextView.setText(getString(R.string.point_location));
        pointLocationAddressTextView.setVisibility(View.VISIBLE);
        String addr = "";
        if (address.getMaxAddressLineIndex() > 0) {
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addr += address.getAddressLine(i) + ", ";
            }
        } else {
            addr = address.getLocality() + ", " + address.getCountryCode();
        }
        pointLocationAddressTextView.setText(addr);
    }
}