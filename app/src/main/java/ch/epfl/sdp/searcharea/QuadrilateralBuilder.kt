package ch.epfl.sdp.searcharea

import ch.epfl.sdp.utils.IntersectionUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class QuadrilateralBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 4
    override val sizeUpperBound: Int? = 4

    protected override fun order() {
        if (isComplete()) {
            val data = vertices

            // Diagonals should intersect
            if (!IntersectionUtils.doIntersect(data[0], data[2], data[1], data[3])) {
                Collections.swap(data, 1, 2)
                if (!IntersectionUtils.doIntersect(data[0], data[2], data[1], data[3])) {
                    Collections.swap(data, 1, 2)
                    Collections.swap(data, 2, 3)
                }
            }
        }
    }

    override fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("Quarilateral not complete")
        }
        return QuadrilateralArea(vertices)
    }
}