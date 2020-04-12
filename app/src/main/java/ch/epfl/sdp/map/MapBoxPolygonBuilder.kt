package ch.epfl.sdp.map

import androidx.lifecycle.LifecycleOwner
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

class MapBoxPolygonBuilder : MapBoxEventManager {
    override fun onMapClicked(position: LatLng) {
        TODO("Not yet implemented")
    }

    override fun mount(lifecycleOwner: LifecycleOwner, mapView: MapView, mapboxMap: MapboxMap, style: Style) {
        TODO("Not yet implemented")
    }

    override fun onMapLongClicked(position: LatLng) {
        TODO("Not yet implemented")
    }
}