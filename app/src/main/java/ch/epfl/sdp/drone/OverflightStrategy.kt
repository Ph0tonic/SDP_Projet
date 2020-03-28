package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.math.floor
import kotlin.math.max

interface OverflightStrategy {
    fun createFlightPath(waypoints: List<LatLng>): List<LatLng>
}

/**
 * Creates a path covering a quadrilateral in several passes
 */
class SimpleMultiPassOnQuadrangle(maxDistBetweenLinesIn: Double) : OverflightStrategy{
    private val maxDistBetweenLines: Double
    init {
        if (maxDistBetweenLinesIn <= 0.0){
            throw java.lang.IllegalArgumentException("The maximum distance between passes must " +
                    "be strictly positive")
        }
        this.maxDistBetweenLines = maxDistBetweenLinesIn
    }
    @Throws(IllegalArgumentException::class)
    override fun createFlightPath(pinpoints: List<LatLng>): List<LatLng> {
        if(pinpoints.size != 4){
            throw IllegalArgumentException("This strategy requires exactly 4 pinpoints, " +
                    "${pinpoints.size} given.")
        }

        // Make a mutable copy of the waypoints to be able to reorder them
        var waypoints = mutableListOf<LatLng>().apply { addAll(pinpoints) }

        // If points were pace not in the right order
        assert(waypoints.size == 4)

        val steps = max(2,floor(max(
                waypoints[0].distanceTo(waypoints[1]) / maxDistBetweenLines,
                waypoints[2].distanceTo(waypoints[3]) / maxDistBetweenLines)).toInt())

        val stepLat1 = (waypoints[1].latitude - waypoints[0].latitude) / (steps - 1)
        val stepLong1 = (waypoints[1].longitude - waypoints[0].longitude)  / (steps - 1)
        val stepLat2 = (waypoints[2].latitude - waypoints[3].latitude)  / (steps - 1)
        val stepLong2 = (waypoints[2].longitude - waypoints[3].longitude)  / (steps - 1)

        val path = ArrayList<LatLng>()

        for (step in 0 until steps){
            if(step % 2 == 0){
                path.add(LatLng(waypoints[0].latitude + step * stepLat1,
                        waypoints[0].longitude + step * stepLong1))
                getStepAlong(waypoints[0], waypoints[1])
                path.add(LatLng(waypoints[3].latitude + step * stepLat2,
                        waypoints[3].longitude + step * stepLong2))
            }else{
                path.add(LatLng(waypoints[3].latitude + step * stepLat2,
                        waypoints[3].longitude + step * stepLong2))
                path.add(LatLng(waypoints[0].latitude + step * stepLat1,
                        waypoints[0].longitude + step * stepLong1))
            }
        }
        return path
    }

    private fun getStepAlong(p0: LatLng, p1: LatLng,
                             step: Int, steps: Int,
                             stepLat: LatLng, stepLng: LatLng): LatLng{
        val stepLat = (p1.latitude - p0.latitude) / (steps - 1)
        val stepLng = (p1.longitude - p0.longitude)  / (steps - 1)
        return LatLng(p0.latitude + step * stepLat, p0.longitude + step * stepLng)
    }
}