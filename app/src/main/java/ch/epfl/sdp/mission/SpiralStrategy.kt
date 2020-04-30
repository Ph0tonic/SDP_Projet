package ch.epfl.sdp.mission

import ch.epfl.sdp.searcharea.CircleArea
import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import net.mastrgamr.mbmapboxutils.SphericalUtil.computeHeading
import net.mastrgamr.mbmapboxutils.SphericalUtil.computeOffset
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt


/**
 * Creates a path covering a quadrilateral in several passes
 */

class SpiralStrategy(maxDistBetweenLinesIn: Double) : OverflightStrategy {
    private val maxDistBetweenLines: Double

    init {
        require(maxDistBetweenLinesIn > 0.0) {
            "The maximum distance between passes must be strictly positive"
        }
        this.maxDistBetweenLines = maxDistBetweenLinesIn
    }

    override fun acceptArea(searchArea: SearchArea): Boolean {
        return searchArea is CircleArea
    }

    override fun createFlightPath(startingPoint: LatLng, searchArea: SearchArea): List<LatLng> {
        require(acceptArea(searchArea)) { "This strategy does not accept this type of area" }
        val area = searchArea as CircleArea
        val center = area.center
        val outer = area.outer
        val radius = center.distanceTo(outer)

        val path = ArrayList<LatLng>()

        val earthRadius = 6378137

        val turns: Double = radius / maxDistBetweenLines

        val maxTheta = radius / earthRadius
        val angleToOuter = Math.toRadians(computeHeading(center, outer))
        val phi0 = angleToOuter - 2 * PI * turns

        //steps is chosen so that the approximate arc length between the two last points is equal to maxDistBetweenLines
        //=> steps is the solution of maxDistBetweenLines = radius * turns * 2 * PI * (1.0 - sqrt((steps-1.0)/steps))
        val protoC = 1.0 - 1 / (turns * turns * 2 * PI)
        val c = max(protoC,0.0) //if distance is short, behaves as a straight line
        val steps = ceil(1.0 / (1 - c * c)).toInt()

        for (step in 0..steps) {
            val s = step.toDouble() / steps
            val t = sqrt(s)
            val theta = maxTheta * t
            val distance = theta * earthRadius
            val phi = 2 * PI * turns * t

            path.add(computeOffset(center, distance, Math.toDegrees(phi0 + phi)))
        }

        val thDist = radius * turns * 2 * PI * (1.0 - sqrt((steps - 1.0) / steps))

        return path
    }
}