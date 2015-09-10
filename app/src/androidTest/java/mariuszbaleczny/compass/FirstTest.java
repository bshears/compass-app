package mariuszbaleczny.compass;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import junit.framework.Assert;

public class FirstTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mFirstTestActivity;
    private TextView mFirstTestText;

    public FirstTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConvert(){
        float angleInRadians = 1f;
        float angleInDegrees = Utils.convertRadiansToDegreesRounded(angleInRadians);
        Assert.assertEquals(angleInDegrees, 57f);
    }
}