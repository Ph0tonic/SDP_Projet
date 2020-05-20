package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng
import net.mastrgamr.mbmapboxutils.SphericalUtil

class QuadrilateralBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 4
    override val sizeUpperBound: Int? = 4
    override val shapeName: String = "Quadrilateral"

    override fun orderVertices() {
        if (isComplete()) {
            val data = vertices
            val first = data[0]
            data.sortBy { SphericalUtil.computeHeading(first, it) }
        }
    }

    override fun buildGivenIsComplete(): QuadrilateralArea = QuadrilateralArea(vertices)
    override fun getShapeVerticesGivenComplete(): List<LatLng> = vertices
}