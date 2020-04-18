package ch.epfl.sdp.drone

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Line
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.utils.ColorUtils

object MissionBuilder {
    private const val PATH_THICKNESS: Float = 2F
    private const val REGION_FILL_OPACITY: Float = 0.5F

    var strategy: OverflightStrategy? = null
    var searchArea: SearchArea? = null
    var startingLocation: LatLng? = null

    var generalObserver: Observer<Any>
//    var latLngObserver: Observer<MutableList<LatLng>>
//    var additionalPropsObserver: Observer<MutableMap<String, Double>>

    var path: List<LatLng>? = null

    private lateinit var lineManager: LineManager
    private lateinit var lineArea: Line

    init {
        generalObserver = Observer {
            computeMissionPath()
        }
    }

    fun mount(lifecycleOwner: LifecycleOwner, mapView: MapView, mapboxMap: MapboxMap, style: Style) {
        lineManager = LineManager(mapView, mapboxMap, style)

        searchArea?.getLatLng()?.observe(lifecycleOwner, this.generalObserver)
        searchArea?.getAdditionalProps()?.observe(lifecycleOwner, this.generalObserver)
    }

    fun changeSearchArea(lifecycleOwner: LifecycleOwner, newSearchArea: SearchArea) {
        searchArea?.getAdditionalProps()?.removeObserver(generalObserver)
        searchArea?.getLatLng()?.removeObserver(generalObserver)

        searchArea = newSearchArea

        searchArea?.getAdditionalProps()?.observe(lifecycleOwner, generalObserver)
        searchArea?.getLatLng()?.observe(lifecycleOwner, generalObserver)

        computeMissionPath()
    }

    fun changeStrategy(lifecycleOwner: LifecycleOwner, newStrategy: OverflightStrategy) {

    }

    fun getMission(): List<LatLng> {
        return path!!
    }

    private fun computeMissionPath() {
        path = strategy?.createFlightPath(startingLocation!!, searchArea!!)
        displayStrategyPath(path!!)
    }

    private fun displayStrategyPath(path: List<LatLng>) {
        if (path.isEmpty()) return

        if (!::lineArea.isInitialized) {
            lineManager.deleteAll()
            val lineOptions = LineOptions()
                    .withLatLngs(path)
                    .withLineWidth(PATH_THICKNESS)
                    .withLineColor(ColorUtils.colorToRgbaString(Color.LTGRAY))
            lineArea = lineManager.create(lineOptions)
        } else {
            lineArea.latLngs = path
            lineManager.update(lineArea)
        }
    }

}
