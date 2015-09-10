package mariuszbaleczny.compass.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import mariuszbaleczny.compass.Constants;
import mariuszbaleczny.compass.Utils;

import static junit.framework.Assert.assertEquals;

public class UtilsTests {

    private List<Float> testData = Arrays.asList(1f, 1f, 1f, 1f, 1f);

    @Test
    public void testLowPassFilterWithNullOutput_shouldReturnInput() {
        float[] input = {1f, 1f, 1f};
        assertEquals(Utils.lowPassFilter(input, null, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT), input);
    }

    @Test
    public void testLowPassFilterWithNullInput_shouldReturnOutput() {
        float[] output = {1f, 1f, 1f};
        assertEquals(Utils.lowPassFilter(null, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT), output);
    }

    @Test
    public void testLowPassFilterOutputLength_shouldBeEqual() {
        float[] input = {1f, 1f, 1f};
        float[] output = {0f, 0f, 0f};
        float[] predictedResult = {0.08f, 0.08f, 0.08f};
        float[] results = Utils.lowPassFilter(input, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
        assertEquals(results.length, predictedResult.length);
    }

    @Test
    public void testLowPassFilterResultDataSetOne_shouldPass() {
        float[] input = {1f, 1f, 1f};
        float[] output = {0f, 0f, 0f};
        float[] predictedResult = {0.08f, 0.08f, 0.08f};
        float[] results = Utils.lowPassFilter(input, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
        Assert.assertArrayEquals(results, predictedResult, 1f);
    }

    @Test
    public void testLowPassFilterResultDataSetTwo() {
        float[] input = {2f, 2f, 2f};
        float[] output = {1.2f, 1.2f, 1.2f};
        float[] predictedResult = {1.264f, 1.264f, 1.264f};
        float[] results = Utils.lowPassFilter(input, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
        Assert.assertArrayEquals(results, predictedResult, 0.001f);
    }

    @Test
    public void testGetMeasurementsAverage() {
        float predictResult = 1f;
        assertEquals(Utils.getMeasurementsAverage(3, testData), predictResult);
    }

    @Test
    public void testNegativeMeasurementsCount_shouldReturnZero() {
        int measurementsCount = -1;
        float predictedResult = 0f;
        assertEquals(Utils.getMeasurementsAverage(measurementsCount, testData), predictedResult);
    }

    @Test
    public void testNullDataInput_shouldReturnZero() {
        float predictResult = 0f;
        assertEquals(Utils.getMeasurementsAverage(1, null), predictResult);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testMeasurementsCountMoreThanDataLength() {
        int measurementsCount = testData.size();
        measurementsCount++;
        float predictedResult = 0f;
        assertEquals(Utils.getMeasurementsAverage(measurementsCount, testData), predictedResult);
    }

    @Test
    public void testLatitudeValueInRange() {
        double valueInRange = 45d;
        assertEquals(Utils.isCoordinateInRange(valueInRange, true), true);
    }

    @Test
    public void testLatitudeValueOutOfRange() {
        double valueOutOfRange = 100d;
        assertEquals(Utils.isCoordinateInRange(valueOutOfRange, true), false);
    }

    @Test
    public void testLatitudeValueMinimum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LATITUDE_MIN, true), true);
    }

    @Test
    public void testLatitudeValueMaximum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LATITUDE_MAX, true), true);
    }

    @Test
    public void testLongitudeValueInRange() {
        double valueOutOfRange = 100d;
        assertEquals(Utils.isCoordinateInRange(valueOutOfRange, false), true);
    }

    @Test
    public void testLongitudeValueOutOfRange() {
        double valueOutOfRange = 200d;
        assertEquals(Utils.isCoordinateInRange(valueOutOfRange, false), false);
    }

    @Test
    public void testLongitudeValueMinimum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LONGITUDE_MIN, false), true);
    }

    @Test
    public void testLongitudeValueMaximum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LONGITUDE_MAX, false), true);
    }

    @Test
    public void testConversionRadiansToDegreesRounded() {
        float valueInRadians = 1f;
        float valueInDegrees = (float) Math.toDegrees(valueInRadians);
        float valueInDegreesRounded = Math.round(valueInDegrees);
        float delta = 1f;
        Assert.assertEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegreesRounded, delta);
    }

    @Test
    public void testConversionRadiansToDegrees() {
        float valueInRadians = 1f;
        float valueInDegrees = (float) Math.toDegrees(valueInRadians);
        float delta = 0.001f;
        Assert.assertNotEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegrees, delta);
    }

}
