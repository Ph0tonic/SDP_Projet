package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng

interface OverflightStrategy {
    fun createFlightPath(pinpoints: List<LatLng>): List<LatLng>
}