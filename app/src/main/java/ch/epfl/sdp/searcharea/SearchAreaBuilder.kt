package ch.epfl.sdp.searcharea

import androidx.annotation.VisibleForTesting
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.properties.Delegates

abstract class SearchAreaBuilder {

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

    fun reset() {
        vertices = mutableListOf()
    }

    fun addVertex(vertex: LatLng): SearchAreaBuilder {
        val isStrictlyUnderBound = sizeUpperBound?.let { vertices.size < it } ?: true
        require(isStrictlyUnderBound) { "Already enough points" }
        vertices.add(vertex)
        order()
        this.vertices = this.vertices
        return this
    }

    fun moveVertex(old: LatLng, new: LatLng): SearchAreaBuilder {
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index
        vertices[oldIndex!!] = new
        order()
        this.vertices = this.vertices
        return this
    }

    protected open fun order() {}

    private fun isUnderUpperBound() = sizeUpperBound?.let { vertices.size <= it } ?: true
    private fun isAboveLowerBound() = sizeLowerBound?.let { it <= vertices.size } ?: true

    fun isComplete(): Boolean {
        return isAboveLowerBound() && isUnderUpperBound()
    }

    abstract fun buildIfComplete(): SearchArea

    fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("$shapeName not complete")
        }
        return buildIfComplete()
    }
}