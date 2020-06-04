package ch.epfl.sdp.map

import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property

class MapboxHomePainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) : MapboxPainter {
    private var symbolManager = SymbolManager(mapView, mapboxMap, style)
    private lateinit var marker: Symbol
    private var reset: Boolean = false

    init {
        symbolManager.iconAllowOverlap = true
        symbolManager.symbolSpacing = 0F
        symbolManager.iconIgnorePlacement = true
        symbolManager.iconRotationAlignment = Property.ICON_ROTATION_ALIGNMENT_VIEWPORT

        style.addImage(R.drawable.ic_home_24dp.toString(), MainApplication.applicationContext().getDrawable(R.drawable.ic_home_24dp)!!)
    }

    fun paint(location: LatLng?) {
        if (location == null) {
            symbolManager.deleteAll()
            reset = true
        } else if (!::marker.isInitialized || reset) {
            val symbolOptions = SymbolOptions()
                    .withLatLng(location)
                    .withIconImage(R.drawable.ic_home_24dp.toString())

            marker = symbolManager.create(symbolOptions)
            reset = false
        } else {
            marker.latLng = location
            symbolManager.update(marker)
        }
    }

    override fun onDestroy() {
        symbolManager.deleteAll()
        symbolManager.onDestroy()
    }
}