package ch.epfl.sdp.searcharea

import androidx.annotation.VisibleForTesting
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.properties.Delegates

abstract class SearchAreaBuilder {

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

    abstract fun addVertex(vertex: LatLng): SearchAreaBuilder
    abstract fun moveVertex(old: LatLng, new: LatLng): SearchAreaBuilder

    abstract fun isComplete(): Boolean
    abstract fun build(): SearchArea
}