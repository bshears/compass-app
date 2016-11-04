package mariuszbaleczny.compass;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class CompassActivity extends AppCompatActivity {

    private boolean pressBackAgainToExit;
    private Toast compassToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        showFragmentIfNotExist(CompassFragment.newInstance(), CompassFragment.FRAGMENT_TAG, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_calibrate) {
            showToastIfInvisible(getString(R.string.calibration_message));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0 || pressBackAgainToExit) {
            super.onBackPressed();
        }
        pressBackAgainToExit = true;
        showToastIfInvisible(getString(R.string.press_again_to_exit_toast));

        new Handler().postDelayed(() -> pressBackAgainToExit = false, Constants.ON_BACK_PRESS_DELAY_TIME);
    }

    private void showFragmentIfNotExist(final Fragment fragment, final String tag, final boolean addToBackStack) {
        final Fragment existingFragment = getSupportFragmentManager().findFragmentById(R.id.container);

        if (isNullOrEqualsTo(fragment, existingFragment)) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (addToBackStack) {
                ft.addToBackStack(null);
            }
            ft.replace(R.id.container, fragment, tag);
            ft.commit();
        }
    }

    private boolean isNullOrEqualsTo(Fragment fragment, Fragment existingFragment) {
        return (existingFragment == null || !fragment.getClass().equals(existingFragment.getClass()));
    }

    private void showToastIfInvisible(CharSequence text) {
        if (isToastNullOrInvisible()) {
            compassToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            compassToast.show();
        }
    }

    private boolean isToastNullOrInvisible() {
        return compassToast == null || compassToast.getView().getWindowVisibility() != View.VISIBLE;
    }
}