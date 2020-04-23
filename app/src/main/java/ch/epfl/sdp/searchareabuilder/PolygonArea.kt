package ch.epfl.sdp.searchareabuilder

import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonArea(val vertices: List<LatLng>) : SearchArea {
    init {
        require(vertices.size >= 3)
    }
}