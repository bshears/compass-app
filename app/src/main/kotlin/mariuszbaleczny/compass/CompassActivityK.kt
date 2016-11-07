package mariuszbaleczny.compass

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

/**
 * Created by mariusz on 31.10.16.
 */
class CompassActivityK : AppCompatActivity() {

    private var toast: Toast? = null
    private var pressBackAgainToExit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)
        showFragment(CompassFragmentK.newInstance(), CompassFragmentK.TAG, false);
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_calibrate) {
            showToastIfInvisible(R.string.calibration_message)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0 || pressBackAgainToExit) {
            return super.onBackPressed()
        }
        pressBackAgainToExit = true
        showToastIfInvisible(R.string.press_again_to_exit_toast)
        Handler().postDelayed({ pressBackAgainToExit = false }, ConstantsK.ON_BACK_PRESS_DELAY_TIME.toLong())
    }

    private fun showFragment(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        val existingFragment = supportFragmentManager.findFragmentById(R.id.container)

        if (existingFragment == null || fragment.javaClass != existingFragment.javaClass) {
            val transaction = supportFragmentManager.beginTransaction()
            if (addToBackStack) {
                transaction.addToBackStack(null)
            }
            transaction.replace(R.id.container, fragment, tag)
            transaction.commit()
        }
    }

    private fun showToastIfInvisible(resId: Int) {
        if (!isToastVisible()) {
            toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    private fun isToastVisible(): Boolean = (toast?.view?.windowVisibility == View.VISIBLE)

}