package ch.epfl.sdp.searcharea

import ch.epfl.sdp.utils.IntersectionUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class QuadrilateralBuilder : SearchAreaBuilder() {

    override fun addVertex(vertex: LatLng): SearchAreaBuilder {
        require(vertices.size < 4) { "Already enough points for a quadrilateral" }
        vertices.add(vertex)
        orderVertex()
        this.vertices = this.vertices
        return this
    }

    override fun moveVertex(old: LatLng, new: LatLng): SearchAreaBuilder {
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index
        vertices.removeAt(oldIndex!!)
        vertices.add(new)
        orderVertex()
        this.vertices = this.vertices
        return this
    }

    private fun orderVertex() {
        if (vertices.size == 4) {
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

    override fun isComplete(): Boolean {
        return vertices.size == 4
    }

    override fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("Quarilateral not complete")
        }
        return QuadrilateralArea(vertices)
    }
}