package ch.epfl.sdp.map

import com.mapbox.mapboxsdk.geometry.LatLng

abstract class MapboxSearchAreaPainter {

    val onMoveVertex = mutableListOf<(old: LatLng, new: LatLng) -> Unit>()

    open fun unMount() {
        onMoveVertex.clear()
    }

    abstract fun paint(vertices: List<LatLng>)
    abstract fun getUpperLayer(): String
}