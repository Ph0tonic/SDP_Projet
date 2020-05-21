package ch.epfl.sdp.searcharea

import androidx.annotation.VisibleForTesting
import ch.epfl.sdp.map.PaintableArea
import com.mapbox.mapboxsdk.geometry.LatLng
import java.lang.IllegalArgumentException
import kotlin.properties.Delegates

abstract class SearchAreaBuilder : PaintableArea {

    abstract val sizeLowerBound: Int?
    abstract val sizeUpperBound: Int?
    abstract val shapeName: String

    val onSearchAreaChanged = mutableListOf<(SearchArea?) -> Unit>()
    val onVerticesChanged = mutableListOf<(MutableList<LatLng>) -> Unit>()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var vertices: MutableList<LatLng> by Delegates.observable(mutableListOf()) { _, _, _ ->
        val searchArea = try {
            this.build()
        } catch (ex: Exception) {
            when (ex) {
                is SearchAreaNotCompleteException -> null
                is IllegalArgumentException -> {
                    val message = ex.message
                    null
                }
                else -> throw ex
            }
        }
        onVerticesChanged.forEach { it(vertices) }
        onSearchAreaChanged.forEach { it(searchArea) }
    }

    fun onDestroy() {
        reset()
        onSearchAreaChanged.clear()
        onVerticesChanged.clear()
    }

    fun reset() {
        vertices.clear()
        this.vertices = this.vertices
    }

    fun addVertex(vertex: LatLng): SearchAreaBuilder {
        require(isStrictlyUnderUpperBound()) { "Already enough points" }
        vertices.add(vertex)
        orderVertices()
        this.vertices = this.vertices
        return this
    }

    fun moveVertex(old: LatLng, new: LatLng): SearchAreaBuilder {
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index
        vertices[oldIndex!!] = new
        orderVertices()
        this.vertices = this.vertices
        return this
    }

    protected open fun orderVertices() {}

    private fun isStrictlyUnderUpperBound() = sizeUpperBound?.let { vertices.size < it } ?: true
    private fun isUnderUpperBound() = sizeUpperBound?.let { vertices.size <= it } ?: true
    private fun isAboveLowerBound() = sizeLowerBound?.let { it <= vertices.size } ?: true

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

    override fun getControlVertices() = vertices

    override fun getShapeVertices(): List<LatLng>? {
        return if (isComplete()) {
            getShapeVerticesGivenComplete()
        } else {
            null
        }
    }

    protected abstract fun getShapeVerticesGivenComplete(): List<LatLng>
}