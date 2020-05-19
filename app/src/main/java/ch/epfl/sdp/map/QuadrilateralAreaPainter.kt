package ch.epfl.sdp.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

class QuadrilateralAreaPainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) :
        PolygonAreaPainter(mapView, mapboxMap, style) {

    override fun paint(vertices: List<LatLng>) {
        require(vertices.size <= 4) {"QuadrilateralArea has more than 4 points"}
        super.paint(vertices)
    }
}