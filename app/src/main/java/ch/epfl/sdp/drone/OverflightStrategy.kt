package ch.epfl.sdp.drone

import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng

interface OverflightStrategy {

    fun acceptArea(searchArea: SearchArea): Boolean

    fun createFlightPath(searchArea: SearchArea): List<LatLng>
}