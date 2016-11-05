package mariuszbaleczny.compass.custom;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import kotlin.Deprecated;

@Deprecated(message = "Replaced with boilerplate-free kotlin language properties")
public class CustomEditTextWatcher implements TextWatcher {

    private final boolean latitude;
    private final OnCoordinateChangeListener listener;

    public CustomEditTextWatcher(final boolean latitude, final OnCoordinateChangeListener listener) {
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
        Double coordinate;
        try {
            coordinate = Double.parseDouble(s.toString());
        } catch (NumberFormatException e) {
            Log.e(getClass().getName(), e.getMessage());
            coordinate = Double.NaN;
        }

        if (listener != null) {
            listener.onCoordinateChanged(latitude, coordinate);
        }
    }

    public interface OnCoordinateChangeListener {
        void onCoordinateChanged(boolean latitude, Double coordinate);
    }
}
