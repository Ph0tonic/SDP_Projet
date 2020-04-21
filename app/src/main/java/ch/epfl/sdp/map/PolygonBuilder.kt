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
        this.vertices = this.vertices
    }

    override fun moveVertex(old: LatLng, new: LatLng) {
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index
        vertices.removeAt(oldIndex!!)
        vertices.add(new)
        this.vertices = this.vertices
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