package mariuszbaleczny.compass.test

import android.location.Location

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import mariuszbaleczny.compass.location.LocationHelperK

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(MockitoJUnitRunner::class)
class LocationHelperTest {

    @Mock
    internal var location: Location? = null

    @Test
    fun shouldBeIncorrectAfterInit() {
        val locationHelper = LocationHelperK(location!!)
        assertFalse(locationHelper.isCorrect())
    }

    @Test
    fun shouldReturnFalseAfterSettingWrongValue() {
        val locationHelper = LocationHelperK(location!!)
        locationHelper.setLatitude(100.0)
        assertFalse(locationHelper.isCorrect())
        locationHelper.setLongitude(-181.0)
        assertFalse(locationHelper.isCorrect())
    }

    @Test
    fun shouldReturnTrueOnCorrectValue() {
        val locationHelper = LocationHelperK(location!!)
        locationHelper.setLatitude(45.0)
        assertFalse(locationHelper.isCorrect())
        locationHelper.setLongitude(-120.0)
        assertTrue(locationHelper.isCorrect())
    }

    @Test
    fun shouldBeCorrectOnProperValues() {
        val locationHelper = LocationHelperK(location!!)
        locationHelper.setLatitude(52.0)
        locationHelper.setLongitude(17.0)
        assertTrue(locationHelper.isCorrect())
    }

    @Test
    fun shouldGetNonNullLocationAfterSettingProperValues() {
        val locationHelper = LocationHelperK(location!!)
        locationHelper.setLatitude(52.0)
        locationHelper.setLongitude(17.0)
        assertNotNull(locationHelper.getLocation())
    }

    @Test
    fun shouldBeNullAfterChangingOnWrongValue() {
        val locationHelper = LocationHelperK(location!!)
        locationHelper.setLatitude(52.0)
        locationHelper.setLongitude(17.0)
        locationHelper.setLatitude(100.0)
        assertNull(locationHelper.getLocation())
    }

}
