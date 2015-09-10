package mariuszbaleczny.compass.custom;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import mariuszbaleczny.compass.CompassToLocationProvider;
import mariuszbaleczny.compass.Utils;

public class CustomEditTextActionEditor implements TextView.OnEditorActionListener {

    private final CustomEditText coordinateEditText;
    private final CompassToLocationProvider compassProvider;
    private final Context context;

    public CustomEditTextActionEditor(Context context,
                                      CustomEditText coordinateEditText,
                                      CompassToLocationProvider compassProvider) {
        this.coordinateEditText = coordinateEditText;
        this.compassProvider = compassProvider;
        this.context = context;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            controlFocus(coordinateEditText, v);
            resetTargetLocationIfEmptyTextView(v);
            return true;
        } else {
            return false;
        }
    }

    private void controlFocus(CustomEditText editText, View view) {
        if (editText.getText().length() != 0) {
            Utils.hideKeyboard(view, context);
        } else {
            editText.requestFocus();
        }
    }

    private void resetTargetLocationIfEmptyTextView(TextView v) {
        if (v.getText().length() == 0) {
            if (compassProvider != null) {
                compassProvider.resetTargetLocation();
            }
        }
    }

}
