package ch.epfl.sdp.mission

import ch.epfl.sdp.searcharea.QuadrilateralArea
import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import net.mastrgamr.mbmapboxutils.SphericalUtil.interpolate
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

/**
 * Creates a path covering a quadrilateral in several passes
 */
class SimpleQuadStrategy(maxDistBetweenLines: Double = DEFAULT_DIST_BETWEEN_LINES) : OverflightStrategy {
    private val maxDistBetweenLines: Double

    companion object {
        const val DEFAULT_DIST_BETWEEN_LINES: Double = 15.0
    }

    init {
        require(maxDistBetweenLines > 0.0) {
            "The maximum distance between passes must be strictly positive"
        }
        this.maxDistBetweenLines = maxDistBetweenLines
    }

    override fun acceptArea(searchArea: SearchArea): Boolean {
        return searchArea is QuadrilateralArea
    }

    @Throws(IllegalArgumentException::class)
    override fun createFlightPath(startingPoint: LatLng, searchArea: SearchArea): List<LatLng> {
        require(acceptArea(searchArea)) { "This strategy does not accept this type of area" }
        val quadrilateralArea = searchArea as QuadrilateralArea
        // Make a mutable copy of the waypoints to be able to reorder them
        val waypointsCopied: MutableList<LatLng> = quadrilateralArea.vertices.toMutableList()
        val startingIndex = waypointsCopied.withIndex().minBy { it.value.distanceTo(startingPoint) }!!.index
        Collections.rotate(waypointsCopied, -startingIndex)

        val (numPointsX, numPointsY) = computeMaxDists(waypointsCopied).toList().map { ceil(it / maxDistBetweenLines).toInt() }

        return (0..numPointsY).flatMap { y ->
            val yPercent = y.toDouble() / numPointsY
            val leftPoint = interpolate(waypointsCopied[0], waypointsCopied[3], yPercent)
            val rightPoint = interpolate(waypointsCopied[1], waypointsCopied[2], yPercent)
            (0..numPointsX).map { x ->
                val xPercent = x.toDouble() / numPointsX
                if (y % 2 == 0) {
                    interpolate(leftPoint, rightPoint, xPercent)
                } else {
                    interpolate(rightPoint, leftPoint, xPercent)
                }
            }
        }
    }

    private fun computeMaxDists(waypoints: List<LatLng>): Pair<Double, Double> {
        val maxDistX = max(                             // 0-1
                waypoints[0].distanceTo(waypoints[1]),  //
                waypoints[3].distanceTo(waypoints[2]))  // 3-2
        val maxDistY = max(                             // 0 1
                waypoints[0].distanceTo(waypoints[3]),  // | |
                waypoints[1].distanceTo(waypoints[2]))  // 3 2
        return Pair(maxDistX, maxDistY)
    }
}