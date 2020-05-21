package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

interface SearchArea {
    val vertices: List<LatLng>
}
