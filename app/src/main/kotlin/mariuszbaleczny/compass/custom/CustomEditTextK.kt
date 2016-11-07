package mariuszbaleczny.compass.custom

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

/**
 * Created by mariusz on 05.11.16.
 */
class CustomEditTextK(context: Context?, attr: AttributeSet?) : EditText(context, attr) {

    constructor(context: Context?) : this(context, null) {
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
            clearFocus()
            return false
        }
        return super.dispatchKeyEvent(event)
    }

}