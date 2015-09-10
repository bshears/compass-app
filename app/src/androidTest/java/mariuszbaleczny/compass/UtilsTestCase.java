package mariuszbaleczny.compass;

import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

public class UtilsTestCase extends AndroidTestCase {

    private List<Float> testData = Arrays.asList(1f, 1f, 1f, 1f, 1f);

    public void testLowPassFilterWithNullOutput_shouldReturnInput() {
        float[] input = {1f, 1f, 1f};
        assertEquals(Utils.lowPassFilter(input, null, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT), input);
    }

    public void testLowPassFilterWithNullInput_shouldReturnOutput() {
        float[] output = {1f, 1f, 1f};
        assertEquals(Utils.lowPassFilter(null, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT), output);
    }

    public void testLowPassFilterOutputLength_shouldBeEqual() {
        float[] input = {1f, 1f, 1f};
        float[] output = {0f, 0f, 0f};
        float[] predictedResult = {0.08f, 0.08f, 0.08f};
        float[] results = Utils.lowPassFilter(input, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
        assertEquals(results.length, predictedResult.length);
    }

    public void testLowPassFilterResultDataSetOne_shouldPass() {
        float[] input = {1f, 1f, 1f};
        float[] output = {0f, 0f, 0f};
        float[] predictedResult = {0.08f, 0.08f, 0.08f};
        float[] results = Utils.lowPassFilter(input, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
        Assert.assertArrayEquals(results, predictedResult, 1f);
    }

    public void testLowPassFilterResultDataSetTwo() {
        float[] input = {2f, 2f, 2f};
        float[] output = {1.2f, 1.2f, 1.2f};
        float[] predictedResult = {1.264f, 1.264f, 1.264f};
        float[] results = Utils.lowPassFilter(input, output, Constants.LOW_PASS_FILTER_SMOOTH_COEFFICIENT);
        Assert.assertArrayEquals(results, predictedResult, 0.001f);
    }

    public void testGetMeasurementsAverage() {
        float predictResult = 1f;
        assertEquals(Utils.getMeasurementsAverage(3, testData), predictResult);
    }

    public void testNegativeMeasurementsCount_shouldReturnZero() {
        int measurementsCount = -1;
        float predictedResult = 0f;
        assertEquals(Utils.getMeasurementsAverage(measurementsCount, testData), predictedResult);
    }

    public void testNullDataInput_shouldReturnZero() {
        float predictResult = 0f;
        assertEquals(Utils.getMeasurementsAverage(1, null), predictResult);
    }


    public void testMeasurementsCountMoreThanDataLength() {
        int measurementsCount = testData.size();
        measurementsCount++;
        float predictedResult = 0f;
        try {
            assertEquals(Utils.getMeasurementsAverage(measurementsCount, testData), predictedResult);
        } catch (IndexOutOfBoundsException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    public void testLatitudeValueInRange() {
        double valueInRange = 45d;
        assertEquals(Utils.isCoordinateInRange(valueInRange, true), true);
    }

    public void testLatitudeValueOutOfRange() {
        double valueOutOfRange = 100d;
        assertEquals(Utils.isCoordinateInRange(valueOutOfRange, true), false);
    }

    public void testLatitudeValueMinimum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LATITUDE_MIN, true), true);
    }

    public void testLatitudeValueMaximum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LATITUDE_MAX, true), true);
    }

    public void testLongitudeValueInRange() {
        double valueOutOfRange = 100d;
        assertEquals(Utils.isCoordinateInRange(valueOutOfRange, false), true);
    }

    public void testLongitudeValueOutOfRange() {
        double valueOutOfRange = 200d;
        assertEquals(Utils.isCoordinateInRange(valueOutOfRange, false), false);
    }

    public void testLongitudeValueMinimum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LONGITUDE_MIN, false), true);
    }

    public void testLongitudeValueMaximum_shouldBeInRange() {
        assertEquals(Utils.isCoordinateInRange(Constants.LONGITUDE_MAX, false), true);
    }

    public void testConversionRadiansToDegreesRounded() {
        float valueInRadians = 1f;
        float valueInDegrees = (float) Math.toDegrees(valueInRadians);
        float valueInDegreesRounded = Math.round(valueInDegrees);
        float delta = 1f;
        assertEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegreesRounded, delta);
    }

    public void testConversionRadiansToDegrees() {
        float valueInRadians = 1f;
        float valueInDegrees = (float) Math.toDegrees(valueInRadians);
        float delta = 0.001f;
        Assert.assertNotEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegrees, delta);
    }

}