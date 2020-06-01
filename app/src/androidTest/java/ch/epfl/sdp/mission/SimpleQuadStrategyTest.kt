package ch.epfl.sdp.mission

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.searcharea.QuadrilateralArea
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import net.mastrgamr.mbmapboxutils.SphericalUtil.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Double.max

@RunWith(AndroidJUnit4::class)
class SimpleQuadStrategyTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test(expected = IllegalArgumentException::class)
    fun doesNotAcceptNegativeMaxDistance() {
        SimpleQuadStrategy(-10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun doesNotAcceptZeroMaxDistance() {
        SimpleQuadStrategy(0.0)
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun doesNotAcceptIncompleteSearchArea() {
        val searchArea = QuadrilateralArea(listOf(LatLng(0.0, 0.0)))
        SimpleQuadStrategy(10.0)
                .createFlightPath(LatLng(0.0, 0.0), searchArea)
    }

    @Test
    fun createsCorrectNumberOfPointsForSmallArea() {
        val searchArea = QuadrilateralArea(listOf(
                LatLng(0.0, 0.0),
                LatLng(1.0, 0.0),
                LatLng(1.0, 1.0),
                LatLng(0.0, 1.0)))
        val strategy = SimpleQuadStrategy(1000000000000.0)
        val path = strategy.createFlightPath(LatLng(0.0, 0.0), searchArea)
        val pathSize = path.size
        assertThat(pathSize, equalTo(4))
    }

    @Test
    fun computeMaxDistOutputsMaximumDistanceHorizontal() {
        val start_side1 = LatLng(0.0, 0.0)
        val start_side2 = LatLng(1.0, 1.0)
        val dist1 = 10.0
        val dist2 = 20.0
        val waypoints = listOf<LatLng>(
                start_side1,
                computeOffset(start_side1, dist1, 19.0),
                start_side2,
                computeOffset(start_side2, dist2, 75.0)
        )
        val res = SimpleQuadStrategy.computeMaxDist(waypoints, SimpleQuadStrategy.Orientation.HORIZONTAL)
        assertThat(res, equalTo(max(dist1, dist2)))
    }

    @Test
    fun computeMaxDistOutputsMaximumDistanceVertical() {
        val startSide1 = LatLng(0.0, 0.0)
        val startSide2 = LatLng(1.0, 1.0)
        val dist1 = 10.0
        val dist2 = 20.0
        val waypoints = listOf<LatLng>(
                startSide1,
                startSide2,
                computeOffset(startSide1, dist2, 75.0),
                computeOffset(startSide2, dist1, 19.0)
        )
        val res = SimpleQuadStrategy.computeMaxDist(waypoints, SimpleQuadStrategy.Orientation.VERTICAL)
        assertThat(res, equalTo(max(dist1, dist2)))
    }

    //TODO test that starting location is taken into account.
}