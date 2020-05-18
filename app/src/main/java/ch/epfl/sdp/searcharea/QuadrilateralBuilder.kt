package ch.epfl.sdp.searcharea

import ch.epfl.sdp.utils.IntersectionUtils
import java.util.*

class QuadrilateralBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 4
    override val sizeUpperBound: Int? = 4
    override val shapeName: String = "Quadrilateral"

    override fun reorderVertices() {
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

    override fun buildGivenIsComplete(): QuadrilateralArea = QuadrilateralArea(vertices)
}