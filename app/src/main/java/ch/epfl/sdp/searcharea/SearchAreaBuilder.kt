package ch.epfl.sdp.searcharea

import androidx.annotation.VisibleForTesting
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.properties.Delegates

abstract class SearchAreaBuilder {

    companion object {
        const val maxDist: Double = 1000.0//meters
    }

    abstract val sizeLowerBound: Int?
    abstract val sizeUpperBound: Int?
    abstract val shapeName: String

    val searchAreaChanged = mutableListOf<(SearchArea?) -> Unit>()
    val verticesChanged = mutableListOf<(MutableList<LatLng>) -> Unit>()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var vertices: MutableList<LatLng> by Delegates.observable(mutableListOf()) { _, _, _ ->
        val searchArea = try {
            this.build()
        } catch (e: SearchAreaNotCompleteException) {
            null
        }
        verticesChanged.forEach { it(vertices) }
        searchAreaChanged.forEach { it(searchArea) }
    }

    fun onDestroy() {
        reset()
        searchAreaChanged.clear()
        verticesChanged.clear()
    }
    
    fun reset() {
        vertices.clear()
        this.vertices = this.vertices
    }

    fun addVertex(vertex: LatLng): SearchAreaBuilder {
        require(isStrictlyUnderUpperBound()) { "Already enough points" }
        require(!isVertexTooFarAway(vertex)) {"Point too far away"}
        vertices.add(vertex)
        reorderVertices()
        this.vertices = this.vertices
        return this
    }

    fun moveVertex(old: LatLng, new: LatLng): SearchAreaBuilder {
        require(!isVertexTooFarAway(new)) {"Point too far away"}
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index
        vertices[oldIndex!!] = new
        reorderVertices()
        this.vertices = this.vertices
        return this
    }

    private fun isVertexTooFarAway(vertex: LatLng): Boolean {
        return vertices.any { vertex.distanceTo(it) > maxDist }
    }

    protected open fun reorderVertices() {}

    private fun isStrictlyUnderUpperBound() = sizeUpperBound?.let { vertices.size <  it } ?: true
    private fun isUnderUpperBound() =         sizeUpperBound?.let { vertices.size <= it } ?: true
    private fun isAboveLowerBound() =         sizeLowerBound?.let { it <= vertices.size } ?: true

    fun isComplete(): Boolean {
        return isAboveLowerBound() && isUnderUpperBound()
    }

    abstract fun buildGivenIsComplete(): SearchArea

    fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("$shapeName not complete")
        }
        return buildGivenIsComplete()
    }
}