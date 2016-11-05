package mariuszbaleczny.compass.custom

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

/**
 * Created by mariusz on 05.11.16.
 */
class CustomEditTextK(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        EditText(context, attrs, defStyleAttr) {

    constructor(context: Context?) : this(context, null, 0) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
            clearFocus()
            return false
        }
        return super.dispatchKeyEvent(event)
    }

}