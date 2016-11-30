package mariuszbaleczny.compass.test.mvp

import android.location.Location
import mariuszbaleczny.compass.R
import mariuszbaleczny.compass.location.CompassPointer
import mariuszbaleczny.compass.location.CoordinateValidator
import mariuszbaleczny.compass.location.LocationHelper
import mariuszbaleczny.compass.mvp.CompassMvp.View
import mariuszbaleczny.compass.mvp.CompassPresenter
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule


/**
 * Created by mariusz on 20.11.16.
 */

class CompassPresenterTest {

    @Rule @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var view: View
    @Mock
    lateinit var compassPointer: CompassPointer

    val presenter: CompassPresenter = CompassPresenter()

    @Before
    fun setUp() {
        presenter.bindView(view)
        presenter.setCompassPointer(compassPointer)
        presenter.locationHelper = LocationHelper(mock(Location::class.java))
    }

    @Test
    fun presenterCreation() {
        Assert.assertNotNull(presenter)
    }

    @Test
    fun viewBindToPresenter() {
        Assert.assertNotNull(presenter.getView())
    }

    @Test
    fun onRotationAnglesReceived() {
        presenter.onCompassPointerRotate(45, 45)

        verify(view).rotateCompass(ArgumentMatchers.eq(45), ArgumentMatchers.eq(45))
    }

    @Test
    fun onSupportedProviderEnabled() {
        presenter.onProviderEnabled("gps")

        verify(view).setCoordinateInputEnabled()
    }

    @Test
    fun onUnsupportedProviderEnabled() {
        presenter.onProviderEnabled("unknown_provider")

        verify(view, never()).setCoordinateInputEnabled()
    }

    @Test
    fun onSupportedProviderDisabled_stillHaveOneEnabled() {
        presenter.onProviderEnabled("gps")
        presenter.onProviderEnabled("network")

        presenter.onProviderDisabled("gps")

        verify(view, never()).setCoordinateInputDisabled()
    }

    @Test
    fun onUnsupportedProvider_noAction() {
        presenter.onProviderDisabled("unknown_provider")
        presenter.onProviderEnabled("unknown_provider")

        verify(view, never()).setCoordinateInputEnabled()
        verify(view, never()).setCoordinateInputDisabled()
    }

    @Test
    fun onLatitudeChanged_nullValue() {
        presenter.onLatitudeChanged(null)

        Assert.assertFalse(presenter.locationHelper.isCorrect())
        verify(view).onNullLatitude()
    }

    @Test
    fun onLatitudeChanged_correctValue() {
        presenter.onLatitudeChanged(45.0)

        Assert.assertFalse(presenter.locationHelper.isCorrect())
        verify(view, never()).onNullLatitude()
        verify(compassPointer).setTargetLocation(ArgumentMatchers.eq(presenter.locationHelper.getLocation()))
        verify(view).onLatitudeInRange()
    }

    @Test
    fun onLatitudeChanged_incorrectValue() {
        presenter.onLatitudeChanged(-200.0)

        Assert.assertFalse(presenter.locationHelper.isCorrect())
        verify(view, never()).onNullLatitude()
        verify(compassPointer).setTargetLocation(ArgumentMatchers.eq(presenter.locationHelper.getLocation()))
        verify(view).onLatitudeOutOfRange()
    }

    @Test
    fun onLongitudeChanged_nullValue() {
        presenter.onLongitudeChanged(null)

        Assert.assertFalse(presenter.locationHelper.isCorrect())
        verify(view).onNullLongitude()
    }

    @Test
    fun onLongitudeChanged_correctValue() {
        presenter.onLongitudeChanged(45.0)

        Assert.assertFalse(presenter.locationHelper.isCorrect())
        verify(view, never()).onNullLongitude()
        verify(compassPointer).setTargetLocation(ArgumentMatchers.eq(presenter.locationHelper.getLocation()))
        verify(view).onLongitudeInRange()
    }

    @Test
    fun onLongitudeChanged_incorrectValue() {
        presenter.onLongitudeChanged(-200.0)

        Assert.assertFalse(presenter.locationHelper.isCorrect())
        verify(view, never()).onNullLongitude()
        verify(compassPointer).setTargetLocation(ArgumentMatchers.eq(presenter.locationHelper.getLocation()))
        verify(view).onLongitudeOutOfRange()
    }

    @Test
    fun onSupportedProviderEnabled_withCorrectLocation() {
        val locationHelper: CoordinateValidator = mock(CoordinateValidator::class.java)
        presenter.locationHelper = locationHelper
        `when`(locationHelper.isCorrect()).thenReturn(true)
        `when`(locationHelper.getLocation()).thenReturn(mock(Location::class.java))

        presenter.onProviderEnabled("gps")

        verify(view).setCoordinateInputEnabled()
        verify(locationHelper).isCorrect()
        verify(compassPointer).setTargetLocation(any(Location::class.java))
        verify(view).setSubtitle(ArgumentMatchers.eq(R.string.info_text_subtitle))
        verify(view).setTitle(ArgumentMatchers.eq(R.string.point_location_title))
    }

    @Test
    fun onSupportedProviderDisabled_noEnabledProviders() {
        val locationHelper: CoordinateValidator = mock(CoordinateValidator::class.java)
        presenter.locationHelper = locationHelper
        presenter.onProviderDisabled("gps")

        verify(view).setCoordinateInputDisabled()
        verify(locationHelper, never()).isCorrect()
        verify(compassPointer, never()).setTargetLocation(any(Location::class.java))
        verify(view).setSubtitle(ArgumentMatchers.eq(R.string.touch_info_error_subtitle))
        verify(view).setTitle(ArgumentMatchers.eq(R.string.needle_free_mode))
    }

}
