package ch.epfl.sdp.mission

import ch.epfl.sdp.searcharea.QuadrilateralArea
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import net.mastrgamr.mbmapboxutils.SphericalUtil.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.lang.Double.max
import kotlin.math.abs

class SimpleQuadStrategyTest {
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
        val startSide1 = LatLng(0.0, 0.0)
        val startSide2 = LatLng(1.0, 1.0)
        val dist1 = 10.0
        val dist2 = 20.0
        val waypoints = listOf<LatLng>(
                startSide1,
                computeOffset(startSide1, dist1, 19.0),
                startSide2,
                computeOffset(startSide2, dist2, 75.0)
        )
        val res = SimpleQuadStrategy.computeMaxDist(waypoints, SimpleQuadStrategy.Orientation.HORIZONTAL)
        val theoryRes = max(dist1, dist2)
        val diff = abs(res - theoryRes)
        assertThat("Wanted $theoryRes, but got: $res", diff < 0.01)
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
                computeOffset(startSide2, dist1, 19.0),
                computeOffset(startSide1, dist2, 75.0)
        )
        val res = SimpleQuadStrategy.computeMaxDist(waypoints, SimpleQuadStrategy.Orientation.VERTICAL)
        val theoryRes = max(dist1, dist2)
        val diff = abs(res - theoryRes)
        assertThat("Wanted $theoryRes, but got: $res", diff < 0.01)
    }

    //TODO test that starting location is taken into account.
}