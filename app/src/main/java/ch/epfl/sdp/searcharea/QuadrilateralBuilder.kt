package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng
import net.mastrgamr.mbmapboxutils.SphericalUtil.*

class QuadrilateralBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 4
    override val sizeUpperBound: Int? = 4
    override val shapeName: String = "Quadrilateral"

    override fun orderVertices() {
        if (isComplete()) {
            val center = middle(vertices[0], vertices[1], vertices[2], vertices[3])
            vertices.sortBy { computeHeading(center, it) }
        }
    }

    private fun middle(a: LatLng, b: LatLng): LatLng = interpolate(a,b, 0.5)
    private fun middle(a: LatLng, b: LatLng, c: LatLng, d: LatLng) = middle(middle(a,b),middle(c,d))

    override fun buildGivenIsComplete(): QuadrilateralArea = QuadrilateralArea(vertices)
    override fun getShapeVerticesGivenComplete(): List<LatLng> = vertices
}