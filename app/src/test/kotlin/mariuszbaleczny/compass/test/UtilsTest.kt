package mariuszbaleczny.compass.test

import mariuszbaleczny.compass.Constants
import mariuszbaleczny.compass.Utils
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun testLatitudeValueInRange() {
        val valueInRange = 45.0
        assertTrue(Utils.isLatitudeInRange(valueInRange))
    }

    @Test
    fun testLatitudeValueOutOfRange() {
        val valueOutOfRange = 100.0
        assertFalse(Utils.isLatitudeInRange(valueOutOfRange))
    }

    @Test
    fun testLatitudeValueMinimum_ShouldBeInRange() {
        assertTrue(Utils.isLatitudeInRange(Constants.LATITUDE_MIN))
    }

    @Test
    fun testLatitudeValueMaximum_ShouldBeInRange() {
        assertTrue(Utils.isLatitudeInRange(Constants.LATITUDE_MAX))
    }

    @Test
    fun shouldBeFalseWhenSetLatitudeDoubleNan() {
        assertFalse(Utils.isLatitudeInRange(java.lang.Double.NaN))
    }

    @Test
    fun testLongitudeValueInRange() {
        val valueOutOfRange = 100.0
        assertTrue(Utils.isLongitudeInRange(valueOutOfRange))
    }

    @Test
    fun testLongitudeValueOutOfRange_shouldReturnFalse() {
        val valueOutOfRange = 200.0
        assertFalse(Utils.isLongitudeInRange(valueOutOfRange))
    }

    @Test
    fun testLongitudeValueMinimum_ShouldBeInRange() {
        assertTrue(Utils.isLongitudeInRange(Constants.LONGITUDE_MIN))
    }

    @Test
    fun testLongitudeValueMaximum_ShouldBeInRange() {
        assertTrue(Utils.isLongitudeInRange(Constants.LONGITUDE_MAX))
    }

    @Test
    fun testConversionRadiansToDegreesRounded_shouldEquals() {
        val valueInRadians = 1f
        val valueInDegrees = Math.toDegrees(valueInRadians.toDouble()).toFloat()
        val valueInDegreesRounded = Math.round(valueInDegrees)
        assertEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegreesRounded)
    }

    @Test
    fun testConversionRadiansToDegrees_shouldNotEquals() {
        val valueInRadians = 1f
        val valueInDegrees = Math.toDegrees(valueInRadians.toDouble()).toFloat()
        Assert.assertNotEquals(Utils.convertRadiansToDegreesRounded(valueInRadians), valueInDegrees)
    }

}
