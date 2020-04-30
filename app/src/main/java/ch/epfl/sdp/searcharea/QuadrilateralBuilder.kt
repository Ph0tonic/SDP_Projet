package ch.epfl.sdp.searcharea

import ch.epfl.sdp.utils.IntersectionUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class QuadrilateralBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 4
    override val sizeUpperBound: Int? = 4
    override val shapeName: String = "Quadrilateral"

    protected override fun order() {
        if (isComplete()) {
            val data = vertices
            fun intersect() = IntersectionUtils.doIntersect(data[0], data[2], data[1], data[3])

            // Diagonals should intersect
            if (!intersect()) {
                Collections.swap(data, 1, 2)
                if (!intersect()) {
                    Collections.swap(data, 1, 2)
                    Collections.swap(data, 2, 3)
                }
            }
        }
    }

    override fun buildIfComplete(): QuadrilateralArea = QuadrilateralArea(vertices)
}