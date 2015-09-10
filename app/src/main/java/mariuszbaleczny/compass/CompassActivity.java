package mariuszbaleczny.compass;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class CompassActivity extends AppCompatActivity {

    private boolean pressBackAgainToExit;
    private Toast onBackPressToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        showFragmentIfNotExist(CompassFragment.newInstance(), CompassFragment.FRAGMENT_TAG, false);
    }

    public void showFragmentIfNotExist(final Fragment fragment, final String tag, final boolean addToBackStack) {
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

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0 || pressBackAgainToExit) {
            super.onBackPressed();
        }
        pressBackAgainToExit = true;
        showToastIfInvisible(getString(R.string.press_again_to_exit_toast));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pressBackAgainToExit = false;
            }
        }, Constants.ON_BACK_PRESS_DELAY_TIME);
    }

    public void showToastIfInvisible(String text) {
        if (isToastVisible()) {
            onBackPressToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            onBackPressToast.show();
        }
    }

    private boolean isToastVisible() {
        return onBackPressToast == null || onBackPressToast.getView().getWindowVisibility() != View.VISIBLE;
    }
}