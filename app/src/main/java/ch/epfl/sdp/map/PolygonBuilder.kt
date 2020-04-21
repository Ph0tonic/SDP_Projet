package ch.epfl.sdp.map

import ch.epfl.sdp.searchareabuilder.PolygonArea
import ch.epfl.sdp.searchareabuilder.SearchArea
import ch.epfl.sdp.searchareabuilder.SearchAreaNotCompleteException
import ch.epfl.sdp.ui.maps.IntersectionTools
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class PolygonBuilder : SearchAreaBuilder() {

    override fun addVertex(vertex: LatLng) {
        vertices.add(vertex)
        orderVertex()
        this.vertices = this.vertices
    }

    override fun moveVertex(old: LatLng, new: LatLng) {
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index
        vertices.removeAt(oldIndex!!)
        vertices.add(new)
        orderVertex()
        this.vertices = this.vertices
    }

    private fun orderVertex() {
        if (vertices.size == 4) {
            val data = vertices

            // Diagonals should intersect
            if (!IntersectionTools.doIntersect(data[0], data[2], data[1], data[3])) {
                Collections.swap(data, 1, 2)
                if (!IntersectionTools.doIntersect(data[0], data[2], data[1], data[3])) {
                    Collections.swap(data, 1, 2)
                    Collections.swap(data, 2, 3)
                }
            }
        }
    }

    override fun isComplete(): Boolean {
        return vertices.size >= 3
    }

    override fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("Quarilateral not complete")
        }
        return PolygonArea(vertices)
    }
}