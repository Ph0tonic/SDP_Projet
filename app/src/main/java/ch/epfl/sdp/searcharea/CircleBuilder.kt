package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class CircleBuilder : SearchAreaBuilder() {


    override fun addVertex(vertex: LatLng): SearchAreaBuilder {
        require(vertices.size < 2) { "Already enough points for a circle" }
        vertices.add(vertex)
        this.vertices = this.vertices
        return this
    }

    override fun moveVertex(old: LatLng, new: LatLng): SearchAreaBuilder {
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index!!
        vertices[oldIndex] = new
        this.vertices = this.vertices
        return this
    }

    override fun isComplete(): Boolean {
        return vertices.size == 2
    }

    override fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("Circle not complete: Needs 2 points")
        }
        return CircleArea(vertices[0], vertices[1])
    }
}