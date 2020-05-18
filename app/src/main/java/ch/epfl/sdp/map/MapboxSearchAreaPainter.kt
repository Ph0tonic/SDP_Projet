package ch.epfl.sdp.map

import com.mapbox.mapboxsdk.geometry.LatLng

abstract class MapboxSearchAreaPainter : MapboxPainter {

    val onMoveVertex = mutableListOf<(old: LatLng, new: LatLng) -> Boolean>()

    override fun onDestroy() {
        onMoveVertex.clear()
    }

    abstract fun paint(vertices: List<LatLng>)
    abstract fun getUpperLayer(): String
}