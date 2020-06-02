package ch.epfl.sdp.mission

import androidx.annotation.VisibleForTesting
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


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    companion object {
        const val DEFAULT_DIST_BETWEEN_LINES: Double = 15.0

        fun computeMaxDist(waypoints: List<LatLng>, orientation: Orientation): Double {
            return if (orientation.isHorizontal()) {
                maxDistWithIndex(waypoints, 0, 1, 3, 2)
            } else {
                maxDistWithIndex(waypoints, 0, 3, 1, 2)
            }
        }

        private fun maxDistWithIndex(waypoints: List<LatLng>, side1v1: Int, side1v2: Int, side2v1: Int, side2v2: Int): Double {
            return max(
                    waypoints[side1v1].distanceTo(waypoints[side1v2]),
                    waypoints[side2v1].distanceTo(waypoints[side2v2]))
        }
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

    enum class Orientation {
        HORIZONTAL {
            override fun isHorizontal() = true
        },
        VERTICAL {
            override fun isHorizontal() = false
        };

        abstract fun isHorizontal(): Boolean
    }

    @Throws(IllegalArgumentException::class)
    override fun createFlightPath(startingPoint: LatLng, searchArea: SearchArea): List<LatLng> {
        require(acceptArea(searchArea)) { "This strategy does not accept this type of area" }
        val quadrilateralArea = searchArea as QuadrilateralArea
        // Make a mutable copy of the waypoints to be able to reorder them
        val waypointsCopied: MutableList<LatLng> = quadrilateralArea.vertices.toMutableList()
        val startingIndex = waypointsCopied.withIndex().minBy { it.value.distanceTo(startingPoint) }!!.index
        Collections.rotate(waypointsCopied, -startingIndex)

        val numPointsY = numPointsFromDist(computeMaxDist(waypointsCopied, Orientation.VERTICAL))

        return (0..numPointsY).flatMap { y ->
            val yPercent = y.toDouble() / numPointsY
            val leftPoint = interpolate(waypointsCopied[0], waypointsCopied[3], yPercent)
            val rightPoint = interpolate(waypointsCopied[1], waypointsCopied[2], yPercent)

            val numPointsX = numPointsFromDist(leftPoint.distanceTo(rightPoint))
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

    private fun numPointsFromDist(dist: Double) = ceil(dist / maxDistBetweenLines).toInt()
}