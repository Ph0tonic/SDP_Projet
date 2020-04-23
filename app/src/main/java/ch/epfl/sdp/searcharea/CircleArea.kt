package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class CircleArea(vertices: List<LatLng>) : SearchArea {
    val center: LatLng
    val radial: LatLng
    init {
        require(vertices.size == 2)
        center = vertices[0]
        radial = vertices[1]
    }
}