package ch.epfl.sdp.drone

import ch.epfl.sdp.searcharea.QuadrilateralArea
import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.max

/**
 * Creates a path covering a quadrilateral in several passes
 */
class SimpleMultiPassOnQuadrilateral(maxDistBetweenLinesIn: Double) : OverflightStrategy {
    private val maxDistBetweenLines: Double

    init {
        require(maxDistBetweenLinesIn > 0.0) {
            "The maximum distance between passes must be strictly positive"
        }
        this.maxDistBetweenLines = maxDistBetweenLinesIn
    }

    override fun acceptArea(searchArea: SearchArea): Boolean {
        return searchArea.isComplete() && (searchArea is QuadrilateralArea)
    }

    @Throws(IllegalArgumentException::class)
    override fun createFlightPath(startingPoint: LatLng, searchArea: SearchArea): List<LatLng> {
        require(acceptArea(searchArea)) { "This strategy does not accept this type of area" }

        // Make a mutable copy of the waypoints to be able to reorder them
        val waypointsCopied = mutableListOf<LatLng>().apply { addAll(searchArea.getLatLng().value!!) }
        val startingIndex = waypointsCopied.withIndex().minBy { it.value.distanceTo(startingPoint) }!!.index
        Collections.rotate(waypointsCopied, -startingIndex)

        val steps = max(2, ceil(max(
                waypointsCopied[0].distanceTo(waypointsCopied[1]) / maxDistBetweenLines,
                waypointsCopied[2].distanceTo(waypointsCopied[3]) / maxDistBetweenLines)).toInt())

        val path = ArrayList<LatLng>()

        for (step in 0 until steps) {
            path.add(generateStepAlong(waypointsCopied[0], waypointsCopied[1], step, steps))
            path.add(generateStepAlong(waypointsCopied[3], waypointsCopied[2], step, steps))
            if (step % 2 != 0) {
                Collections.swap(path, path.size - 1, path.size - 2)
            }
        }
        return path
    }

    /**
     * Generates a LatLng positioned at step/steps in the segment p0---p1
     */
    private fun generateStepAlong(p0: LatLng, p1: LatLng, step: Int, steps: Int): LatLng {
        val stepLat = (p1.latitude - p0.latitude) / (steps - 1)
        val stepLng = (p1.longitude - p0.longitude) / (steps - 1)
        return LatLng(p0.latitude + step * stepLat, p0.longitude + step * stepLng)
    }
}