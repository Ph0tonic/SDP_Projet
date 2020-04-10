package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

interface SearchArea {

    fun isComplete(): Boolean
    fun getLatLng(): List<LatLng>
}