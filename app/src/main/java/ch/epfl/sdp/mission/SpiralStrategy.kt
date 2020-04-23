package ch.epfl.sdp.mission

import ch.epfl.sdp.searcharea.CircleArea
import ch.epfl.sdp.searcharea.SearchArea
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

    override fun acceptArea(searchArea: SearchArea): Boolean {
        return searchArea is CircleArea
    }

    override fun createFlightPath(startingPoint: LatLng, searchArea: SearchArea): List<LatLng> {
        require(acceptArea(searchArea)) { "This strategy does not accept this type of area" }
        val area = searchArea as CircleArea
        val center = area.center
        val outter = area.radial
        val radius = center.distanceTo(outter)

        val path = ArrayList<LatLng>()

        val steps = 200

        val earthRadius = 6378137

        val turns: Double = ceil(radius/maxDistBetweenLines)

        val maxTheta = radius/earthRadius
        val phi0 = computeHeading(center,outter)

        for(step in 0 .. steps){
            val s = step.toDouble()/steps //between 0 and 1
            val t = sqrt(s)//sqrt(2*s/a)
            val theta = maxTheta*t
            val distance = theta*earthRadius
            val phi = turns * 2 * PI * t

            path.add(computeOffset(center,distance, phi0 + Math.toDegrees(phi)))
        }

        return path
    }
}