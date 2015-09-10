package mariuszbaleczny.compass.Custom;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import mariuszbaleczny.compass.CompassToLocationProvider;
import mariuszbaleczny.compass.Utils;

public class CustomEditTextActionEditor implements TextView.OnEditorActionListener {

    private final boolean latitude;
    private final CustomEditText latitudeEditText;
    private final CustomEditText longitudeEditText;
    private final CompassToLocationProvider compassProvider;
    private final Context context;

    public CustomEditTextActionEditor(Context context,
                                      CustomEditText latitudeEditText,
                                      CustomEditText longitudeEditText,
                                      CompassToLocationProvider compassProvider,
                                      boolean latitude) {
        this.latitude = latitude;
        this.latitudeEditText = latitudeEditText;
        this.longitudeEditText = longitudeEditText;
        this.compassProvider = compassProvider;
        this.context = context;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (latitude) {
                controlFocus(longitudeEditText, v, context);
            } else {
                controlFocus(latitudeEditText, v, context);
            }
            resetTargetLocationIfEmptyTextView(v, compassProvider);
            return true;
        } else {
            return false;
        }
    }

    public void controlFocus(CustomEditText editText, View view, Context context) {
        if (editText.getText().length() != 0) {
            Utils.hideKeyboard(view, context);
        } else {
            editText.requestFocus();
        }
    }

    public void resetTargetLocationIfEmptyTextView(TextView v, CompassToLocationProvider provider) {
        if (v.getText().length() == 0) {
            provider.resetTargetLocation();
        }
    }

}
