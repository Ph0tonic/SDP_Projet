package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import net.mastrgamr.mbmapboxutils.SphericalUtil.computeHeading
import net.mastrgamr.mbmapboxutils.SphericalUtil.computeOffset
import kotlin.collections.ArrayList
import kotlin.math.*


/**
 * Creates a path covering a quadrilateral in several passes
 */
class SpiralStrategy(maxDistBetweenLinesIn: Double) : OverflightStrategy {
    private val maxDistBetweenLines: Double

    companion object Constraints {
        const val pinPointsAmount = 2
    }

    init {
        require(maxDistBetweenLinesIn > 0.0) {
            "The maximum distance between passes must be strictly positive"
        }
        this.maxDistBetweenLines = maxDistBetweenLinesIn
    }

    @Throws(IllegalArgumentException::class)
    override fun createFlightPath(pinpoints: List<LatLng>): List<LatLng> {
        require(pinpoints.size == pinPointsAmount) {
            "This strategy requires exactly $pinPointsAmount pinpoints, ${pinpoints.size} given."
        }

        // Make a mutable copy of the waypoints to be able to reorder them
        /*
        val waypointsCopied = mutableListOf<LatLng>().apply { addAll(pinpoints) }

        val steps = max(2, ceil(max(
                waypointsCopied[0].distanceTo(waypointsCopied[1]) / maxDistBetweenLines,
                waypointsCopied[2].distanceTo(waypointsCopied[3]) / maxDistBetweenLines)).toInt())

         */
        val center = pinpoints[0]
        val outter = pinpoints[1]
        val radius = center.distanceTo(outter)

        val path = ArrayList<LatLng>()

        val steps = 200
        /*
        for(step in 0 .. steps){
            path.add(computeOffset(center,radius,360.0 * step/steps))
        }

         */

        val earthRadius = 6378137

        val turns: Double = 4.0

        val maxTheta = radius/earthRadius
        val phi0 = computeHeading(center,outter)
        
        for(step in 0 .. steps){
            val theta = maxTheta*step/steps
            val distance = theta*earthRadius
            val phi = turns * 2* PI * step/steps



            /*
            val aMax = PI*turns/2
            val a = (step/steps)*aMax

            val theta = (PI/2)*a/aMax //PI*step/steps
            val distance = theta*realRadius
            val phi = step.toDouble()//a //(step/steps)*turns*PI/2

             */

            path.add(computeOffset(center,distance, phi0 + Math.toDegrees(phi)))
        }

        return path
    }
}