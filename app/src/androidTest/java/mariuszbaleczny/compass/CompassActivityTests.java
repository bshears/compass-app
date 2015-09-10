package mariuszbaleczny.compass;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class CompassActivityTests extends ActivityInstrumentationTestCase2<CompassActivity> {

    private CompassActivity compassActivity;
    private TextView titleTextView;

    public CompassActivityTests() {
        super(CompassActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        compassActivity = getActivity();
        titleTextView = (TextView) compassActivity.findViewById(R.id.fragment_compass_title_text_view);
    }

    public void testPreconditions() {
        assertNotNull("CompassActivity is null", compassActivity);
        assertNotNull(Utils.isCompassSensorPresent(compassActivity));
    }

    public void testTitleWhenCompassSensorIsNotPresent() {
        String title = titleTextView.getText().toString();
        assertEquals(compassActivity.getString(R.string.compass_not_detected_title), title);
    }

}
