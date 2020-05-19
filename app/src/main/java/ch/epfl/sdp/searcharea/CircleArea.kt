package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class CircleArea(val center: LatLng, val outer: LatLng) : SearchArea {
    override val vertices: List<LatLng>
        get() = listOf(center, outer)
}