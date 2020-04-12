package ch.epfl.sdp.map

import com.mapbox.mapboxsdk.geometry.LatLng

interface MapBoxEventManager {
    fun onMapLongClicked(position: LatLng)
    fun onMapClicked(position: LatLng)
}