package mariuszbaleczny.compass.test;

import org.junit.Assert;
import org.junit.Test;

import mariuszbaleczny.compass.Constants;
import mariuszbaleczny.compass.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testLatitudeValueInRange() {
        double valueInRange = 45d;
        assertTrue(Utils.isCoordinateInRange(valueInRange, true));
    }

    @Test
    public void testLatitudeValueOutOfRange() {
        double valueOutOfRange = 100d;
        assertFalse(Utils.isCoordinateInRange(valueOutOfRange, true));
    }

    @Test
    public void testLatitudeValueMinimum_ShouldBeInRange() {
        assertTrue(Utils.isCoordinateInRange(Constants.LATITUDE_MIN, true));
    }

    @Test
    public void testLatitudeValueMaximum_ShouldBeInRange() {
        assertTrue(Utils.isCoordinateInRange(Constants.LATITUDE_MAX, true));
    }

    @Test
    public void shouldBeFalseWhenSetLatitudeDoubleNan(){
        assertFalse(Utils.isCoordinateInRange(Double.NaN, true));
    }

    @Test
    public void testLongitudeValueInRange() {
        double valueOutOfRange = 100d;
        assertTrue(Utils.isCoordinateInRange(valueOutOfRange, false));
    }

    @Test
    public void testLongitudeValueOutOfRange_shouldReturnFalse() {
        double valueOutOfRange = 200d;
        assertFalse(Utils.isCoordinateInRange(valueOutOfRange, false));
    }

    @Test
    public void testLongitudeValueMinimum_ShouldBeInRange() {
        assertTrue(Utils.isCoordinateInRange(Constants.LONGITUDE_MIN, false));
    }

    @Test
    public void testLongitudeValueMaximum_ShouldBeInRange() {
        assertTrue(Utils.isCoordinateInRange(Constants.LONGITUDE_MAX, false));
    }

    @Test
    public void testConversionRadiansToDegreesRounded_shouldEquals() {
        float valueInRadians = 1f;
        float valueInDegrees = (float) Math.toDegrees(valueInRadians);
        int valueInDegreesRounded = Math.round(valueInDegrees);
        assertEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegreesRounded);
    }

    @Test
    public void testConversionRadiansToDegrees_shouldNotEquals() {
        float valueInRadians = 1f;
        float valueInDegrees = (float) Math.toDegrees(valueInRadians);
        float delta = 0.001f;
        Assert.assertNotEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegrees, delta);
    }

}
