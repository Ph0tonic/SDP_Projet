package ch.epfl.sdp.map

import com.mapbox.mapboxsdk.geometry.LatLng

abstract class MapboxSearchAreaPainter : MapboxPainter {

    val onVertexMoved = mutableListOf<(old: LatLng, new: LatLng) -> Unit>()

    override fun onDestroy() {
        onVertexMoved.clear()
    }

    abstract fun paint(vertices: List<LatLng>)
    abstract fun getUpperLayer(): String
}