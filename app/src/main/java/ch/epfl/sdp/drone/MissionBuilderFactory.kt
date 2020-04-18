package ch.epfl.sdp.drone

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone.MissionBuilder.searchArea
import ch.epfl.sdp.map.MapBoxSearchAreaBuilder
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

object MissionBuilderFactory {

    private const val PATH_THICKNESS: Float = 2F
    private const val REGION_FILL_OPACITY: Float = 0.5F

    private var strategy: OverflightStrategy? = null

    private lateinit var lineManager: LineManager
    private lateinit var lineArea: Line

    fun changeStrategy() {
        val path = strategy?.createFlightPath(searchArea!!)
        displayStrategyPath(path)
    }

    fun changeSearchAreaBuilder() {

    }

    fun mount(lifecycleOwner: LifecycleOwner, mapView: MapView, mapboxMap: MapboxMap, style: Style) {
        lineManager = LineManager(mapView, mapboxMap, style)

        val observer = Observer<Any> {

        }
        searchArea?.getLatLng()?.observe(lifecycleOwner, this.observer)

        circleManager.addDragListener(object : OnCircleDragListener {
            lateinit var location: LatLng
            override fun onAnnotationDragStarted(annotation: Circle) {
                movingWaypoint = true
                location = annotation.latLng
            }

            override fun onAnnotationDrag(annotation: Circle) {
                searchArea.moveAngle(location, annotation.latLng)
                location = annotation.latLng
            }

            override fun onAnnotationDragFinished(annotation: Circle) {
                movingWaypoint = false
            }
        })
    }

    fun displayStrategyPath(path: List<LatLng>) {
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