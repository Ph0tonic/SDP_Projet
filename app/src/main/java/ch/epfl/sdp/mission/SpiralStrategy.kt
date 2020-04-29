package ch.epfl.sdp.mission

import android.widget.Toast
import ch.epfl.sdp.searcharea.CircleArea
import ch.epfl.sdp.searcharea.SearchArea
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import net.mastrgamr.mbmapboxutils.SphericalUtil.computeHeading
import net.mastrgamr.mbmapboxutils.SphericalUtil.computeOffset
import timber.log.Timber
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




        val earthRadius = 6378137

        val turns: Double = ceil(radius/maxDistBetweenLines)

        val maxTheta = radius/earthRadius
        val phi0 = computeHeading(center,outter)

        //steps is the solution of {thDist = radius * turns * 2 * PI * (1.0 - sqrt((steps-1.0)/steps))} for thDist = maxDistBetweenLines
        val c = 1.0 - maxDistBetweenLines/(radius*turns*2*PI)
        val steps = ceil(1.0/(1-c*c)).toInt()

        for(step in 0 .. steps){
            val s = step.toDouble()/steps //between 0 and 1
            val t = sqrt(s)
            val theta = maxTheta*t
            val distance = theta*earthRadius
            val phi = turns * 2 * PI * t

            path.add(computeOffset(center,distance, phi0 + Math.toDegrees(phi)))
        }

        val thDist = radius * turns * 2 * PI * (1.0 - sqrt((steps-1.0)/steps))

        return path
    }
}