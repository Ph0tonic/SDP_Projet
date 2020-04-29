package ch.epfl.sdp.mission

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.mission.SimpleMultiPassOnQuadrilateral
import ch.epfl.sdp.searcharea.CircleArea
import ch.epfl.sdp.searcharea.QuadrilateralArea
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.Circle
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpiralStrategyTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test(expected = IllegalArgumentException::class)
    fun doesNotAcceptNegativeMaxDistance() {
        SpiralStrategy(-10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun doesNotAcceptZeroMaxDistance() {
        SpiralStrategy(0.0)
    }

    @Test
    fun simpleMultiPassCreatesGoodNumberOfPointsForSmallArea() {
        val searchArea = CircleArea(
                LatLng(0.0, 0.0),
                LatLng(1.0, 0.0)
        )
        val strategy = SpiralStrategy(1000.0)
        val path = strategy.createFlightPath(LatLng(0.0, 0.0), searchArea)
        val pathSize = path.size
        assertThat(pathSize, equalTo(2))
    }
}