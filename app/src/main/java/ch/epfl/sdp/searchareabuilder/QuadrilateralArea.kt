package ch.epfl.sdp.searchareabuilder

import com.mapbox.mapboxsdk.geometry.LatLng

class QuadrilateralArea(val vertices: List<LatLng>) : SearchArea {
    init {
        require(vertices.size == 4)
    }
}