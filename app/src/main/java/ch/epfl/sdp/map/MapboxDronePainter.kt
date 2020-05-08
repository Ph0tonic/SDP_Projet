package ch.epfl.sdp.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Circle
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.utils.ColorUtils

class MapboxDronePainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) {
    private var circleManager = CircleManager(mapView, mapboxMap, style)
    private lateinit var marker: Circle
    private var reset: Boolean = false

    fun paint(location: LatLng?) {
        if (location == null) {
            circleManager.deleteAll()
            reset = true
        } else if (!::marker.isInitialized || reset) {
            val circleOptions = CircleOptions()
                    .withLatLng(location)
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.RED))
            marker = circleManager.create(circleOptions)
            reset = false
        } else {
            marker.latLng = location
            circleManager.update(marker)
        }
    }
}