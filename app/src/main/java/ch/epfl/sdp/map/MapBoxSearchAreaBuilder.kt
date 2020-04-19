package ch.epfl.sdp.map

import androidx.lifecycle.LifecycleOwner
import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

interface MapBoxSearchAreaBuilder {

    fun onMapLongClicked(position: LatLng)
    fun onMapClicked(position: LatLng)

    fun mount(lifecycleOwner: LifecycleOwner, mapView: MapView, mapboxMap: MapboxMap, style: Style)
    fun unMount()

    fun resetSearchArea()

    fun searchArea(): SearchArea
}