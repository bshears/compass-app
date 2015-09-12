package mariuszbaleczny.compass.custom;

import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import mariuszbaleczny.compass.Utils;

public class CustomEditTextActionEditor implements TextView.OnEditorActionListener {

    private final Context context;
    private final CustomEditText complementaryCoordinateEditText;

    public CustomEditTextActionEditor(Context context, CustomEditText complementaryCoordinateEditText) {
        this.complementaryCoordinateEditText = complementaryCoordinateEditText;
        this.context = context;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            controlFocus(complementaryCoordinateEditText, v);
            return true;
        } else {
            return false;
        }
    }

    private void controlFocus(CustomEditText editText, View view) {
        if (!TextUtils.isEmpty(editText.getText())) {
            Utils.hideKeyboard(view, context);
            view.clearFocus();
        } else {
            editText.requestFocus();
        }
    }

}
