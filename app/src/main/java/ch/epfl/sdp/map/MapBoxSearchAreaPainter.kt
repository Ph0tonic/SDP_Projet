package ch.epfl.sdp.map

import com.mapbox.mapboxsdk.geometry.LatLng

abstract class MapBoxSearchAreaPainter {

    val onMoveVertex = mutableListOf<(old: LatLng, new: LatLng) -> Unit>()

    abstract fun unMount()
    abstract fun paint(vertices: List<LatLng>)
}