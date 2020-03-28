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

        val passes = max(2,floor(max(
                waypoints[0].distanceTo(waypoints[1]) / maxDistBetweenLines,
                waypoints[2].distanceTo(waypoints[3]) / maxDistBetweenLines)).toInt())

        val stepLat1 = (waypoints[1].latitude - waypoints[0].latitude) / (passes - 1)
        val stepLong1 = (waypoints[1].longitude - waypoints[0].longitude)  / (passes - 1)
        val stepLat2 = (waypoints[2].latitude - waypoints[3].latitude)  / (passes - 1)
        val stepLong2 = (waypoints[2].longitude - waypoints[3].longitude)  / (passes - 1)

        val path = ArrayList<LatLng>()

        for (pass in 0 until passes){
            if(pass % 2 == 0){
                path.add(LatLng(waypoints[0].latitude + pass * stepLat1,
                        waypoints[0].longitude + pass * stepLong1))
                path.add(LatLng(waypoints[3].latitude + pass * stepLat2,
                        waypoints[3].longitude + pass * stepLong2))
            }else{
                path.add(LatLng(waypoints[3].latitude + pass * stepLat2,
                        waypoints[3].longitude + pass * stepLong2))
                path.add(LatLng(waypoints[0].latitude + pass * stepLat1,
                        waypoints[0].longitude + pass * stepLong1))
            }
        }
        return path
    }
}