package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng
import net.mastrgamr.mbmapboxutils.SphericalUtil
import kotlin.math.*

class CircleBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 2
    override val sizeUpperBound: Int? = 2
    override val shapeName: String = "Circle"
    override fun buildGivenIsComplete() = CircleArea(center, outer)

    val center
        get() = vertices[0]
    val outer
        get() = vertices[1]
    val radius
        get() = center.distanceTo(outer)

    override fun getShapeVerticesGivenComplete(): List<LatLng> {
        val sides = 45
        val numberOfPoints = sides
        return List<LatLng>(sides) {
            val percent = it / numberOfPoints.toDouble()
            SphericalUtil.computeOffset(center, radius, percent * 360)
        }
    }
}