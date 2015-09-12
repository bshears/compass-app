package mariuszbaleczny.compass.test;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import mariuszbaleczny.compass.location.LocationHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LocationHelperTest {

    @Mock
    Location location;

    @Test
    public void shouldBeIncorrectAfterInit() {
        LocationHelper locationHelper = new LocationHelper(location);
        assertFalse(locationHelper.isCorrect());
    }

    @Test
    public void shouldReturnFalseAfterSettingWrongValue() {
        LocationHelper locationHelper = new LocationHelper(location);
        boolean latitudeSucceeded = locationHelper.setLatitude(100d);
        assertFalse(latitudeSucceeded);
        boolean longitudeSucceeded = locationHelper.setLongitude(-181d);
        assertFalse(longitudeSucceeded);
    }

    @Test
    public void shouldReturnTrueOnCorrectValue() {
        LocationHelper locationHelper = new LocationHelper(location);
        boolean latitudeSucceeded = locationHelper.setLatitude(45d);
        assertTrue(latitudeSucceeded);
        boolean longitudeSucceeded = locationHelper.setLongitude(-120d);
        assertTrue(longitudeSucceeded);
    }

    @Test
    public void shouldBeCorrectOnProperValues() {
        LocationHelper locationHelper = new LocationHelper(location);
        locationHelper.setLatitude(52d);
        locationHelper.setLongitude(17d);
        assertTrue(locationHelper.isCorrect());
    }

    @Test
    public void shouldGetNonNullLocationAfterSettingProperValues() {
        LocationHelper locationHelper = new LocationHelper(location);
        locationHelper.setLatitude(52d);
        locationHelper.setLongitude(17d);
        assertNotNull(locationHelper.getLocation());
    }

    @Test
    public void shouldBeNullAfterChangingOnWrongValue() {
        LocationHelper locationHelper = new LocationHelper(location);
        locationHelper.setLatitude(52d);
        locationHelper.setLongitude(17d);
        locationHelper.setLatitude(100d);
        assertNull(locationHelper.getLocation());
    }

}
