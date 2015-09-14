package mariuszbaleczny.compass;

import android.app.Instrumentation;
import android.support.design.widget.TextInputLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.ImageView;
import android.widget.TextView;

import mariuszbaleczny.compass.custom.CustomEditText;

public class CompassActivityTest extends ActivityInstrumentationTestCase2<CompassActivity> {

    private CompassActivity compassActivity;
    private Instrumentation instrumentation;
    private CustomEditText latitudeEditText;
    private CustomEditText longitudeEditText;
    private TextInputLayout latitudeTextInputLayout;
    private TextInputLayout longitudeTextInputLayout;
    private TextView titleTextView;
    private ImageView compassRose;
    private ImageView compassNeedle;

    public CompassActivityTest() {
        super(CompassActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        compassActivity = getActivity();
        instrumentation = getInstrumentation();
        latitudeEditText = (CustomEditText) compassActivity.findViewById(R.id.fragment_compass_latitude_edit_text);
        longitudeEditText = (CustomEditText) compassActivity.findViewById(R.id.fragment_compass_longitude_edit_text);
        latitudeTextInputLayout = (TextInputLayout) compassActivity.findViewById(R.id.fragment_compass_latitude_text_input);
        longitudeTextInputLayout = (TextInputLayout) compassActivity.findViewById(R.id.fragment_compass_longitude_text_input);
        titleTextView = (TextView) compassActivity.findViewById(R.id.fragment_compass_title_text_view);
        compassRose = (ImageView) compassActivity.findViewById(R.id.fragment_compass_rose);
        compassNeedle = (ImageView) compassActivity.findViewById(R.id.fragment_compass_needle);
    }

    public void testPreconditions() {
        assertNotNull("CompassActivity is null", compassActivity);
        assertNotNull("latitudeEditText is null", latitudeEditText);
        assertNotNull("longitudeEditText is null", longitudeEditText);
        assertNotNull("latitudeTextInputLayout is null", latitudeTextInputLayout);
        assertNotNull("longitudeTextInputLayout is null", longitudeTextInputLayout);
        assertNotNull("titleTextView is null", titleTextView);
        assertNotNull("compassRose is null", compassRose);
        assertNotNull("compassNeedle is null", compassNeedle);
    }

    @UiThreadTest
    public void testCoordinatesEditTextFocus() {
        assertNotNull(latitudeEditText);
        assertTrue(latitudeEditText.requestFocus());
        assertTrue(latitudeEditText.hasFocus());
        assertNotNull(longitudeEditText);
        assertTrue(longitudeEditText.requestFocus());
        assertTrue(longitudeEditText.hasFocus());
    }

    @UiThreadTest
    public void testCoordinatesEditTextInputValue() {
        assertTrue(latitudeEditText.requestFocus());
        latitudeEditText.setText("52");
        assertTrue(longitudeEditText.requestFocus());
        longitudeEditText.setText("17");
        assertEquals("52", latitudeEditText.getText().toString());
        assertEquals("17", longitudeEditText.getText().toString());
    }

    @UiThreadTest
    public void testCoordinatesEditTextErrorOnWrongValue() {
        assertTrue(latitudeEditText.requestFocus());
        latitudeEditText.setText("200");
        assertTrue(longitudeEditText.requestFocus());
        longitudeEditText.setText("200");
        assertEquals(compassActivity.getString(R.string.error_latitude_out_of_range), latitudeTextInputLayout.getError());
        assertEquals(compassActivity.getString(R.string.error_longitude_out_of_range), longitudeTextInputLayout.getError());
    }

    @UiThreadTest
    public void testTitleOnCorrectInput(){
        compassActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                latitudeEditText.setText("52");
                longitudeEditText.setText("17");
            }
        });
        instrumentation.waitForIdleSync();
        assertEquals(compassActivity.getString(R.string.point_location_title), titleTextView.getText().toString());
    }

    public void testTitleOnIncorrectInput(){
        compassActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                latitudeEditText.setText("52");
                longitudeEditText.setText("");
            }
        });
        instrumentation.waitForIdleSync();
        assertEquals(compassActivity.getString(R.string.point_north_title), titleTextView.getText().toString());
    }

}
