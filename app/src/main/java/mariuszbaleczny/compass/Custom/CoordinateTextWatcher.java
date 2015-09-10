package mariuszbaleczny.compass.custom;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import mariuszbaleczny.compass.Utils;

public class CoordinateTextWatcher implements TextWatcher {

    private final boolean latitude;
    private final OnCoordinateChangeListener listener;
    private double coordinate;

    public CoordinateTextWatcher(final boolean latitude, final OnCoordinateChangeListener listener) {
        this.latitude = latitude;
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            coordinate = Double.parseDouble(s.toString());
            if (!Utils.isCoordinateInRange(coordinate, latitude)) {
                coordinate = Double.NaN;
            }
        } catch (NumberFormatException e) {
            Log.e(getClass().getName(), e.getMessage());
        }

        if (listener != null) {
            if (TextUtils.isEmpty(s) || !Utils.isCoordinateInRange(coordinate, latitude)) {
                listener.onEmptyOrWrongInput(latitude, !Utils.isCoordinateInRange(coordinate, latitude));
            } else {
                listener.onCoordinateChanged(latitude, coordinate);
            }
        }
    }

    public interface OnCoordinateChangeListener {
        void onCoordinateChanged(boolean latitude, double coordinate);

        void onEmptyOrWrongInput(boolean latitude, boolean outOfRange);
    }
}
