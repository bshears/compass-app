package mariuszbaleczny.compass;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class CompassActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        if (getSupportFragmentManager().findFragmentById(R.id.container) != null) {
            showFragmentIfNotExist(CompassFragment.newInstance(), CompassFragment.FRAGMENT_TAG, false);
        }
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

}