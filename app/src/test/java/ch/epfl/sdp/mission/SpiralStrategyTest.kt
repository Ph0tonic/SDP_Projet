package ch.epfl.sdp.mission

import ch.epfl.sdp.searcharea.CircleArea
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SpiralStrategyTest {

    @Test(expected = IllegalArgumentException::class)
    fun doesNotAcceptNegativeMaxDistance() {
        SpiralStrategy(-10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun doesNotAcceptZeroMaxDistance() {
        SpiralStrategy(0.0)
    }

    @Test
    fun createsGoodNumberOfPointsForSmallArea() {
        val searchArea = CircleArea(
                LatLng(0.0, 0.0),
                LatLng(1.0, 0.0)
        )
        val strategy = SpiralStrategy(1000000000000.0)
        val path = strategy.createFlightPath(LatLng(0.0, 0.0), searchArea)
        assertThat(path.size, equalTo(2))
    }
}