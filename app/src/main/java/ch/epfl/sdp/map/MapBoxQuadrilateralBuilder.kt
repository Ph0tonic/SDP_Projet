package ch.epfl.sdp.map

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ch.epfl.sdp.searcharea.QuadrilateralArea
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

class MapBoxQuadrilateralBuilder(private val searchArea: QuadrilateralArea) : MapBoxEventManager {

    override fun onMapClicked(position: LatLng) {
        if (!searchArea.isComplete()) searchArea.addAngle(position)
    }

    override fun onMapLongClicked(position: LatLng) {

    }

    companion object {
        private const val PATH_THICKNESS: Float = 5F
        private const val REGION_FILL_OPACITY: Float = 0.5F
    }

    private lateinit var circleManager: CircleManager
    private lateinit var lineManager: LineManager
    private lateinit var fillManager: FillManager

    private var movingWaypoint: Boolean = false

    override fun mount(lifecycleOwner: LifecycleOwner, mapView: MapView, mapboxMap: MapboxMap, style: Style) {

        fillManager = FillManager(mapView, mapboxMap, style)
        lineManager = LineManager(mapView, mapboxMap, style)
        circleManager = CircleManager(mapView, mapboxMap, style)

        searchArea.getLatLng().observe(lifecycleOwner, Observer {
            //Create a marker for each point
//            drawPath(it)
            drawRegion(it)
            drawPinpoint(it)
        })

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

    /**
     * Draws the path given by the list of positions
     */
    private fun drawPath(path: List<LatLng>) {
        lineManager.create(LineOptions()
                .withLatLngs(path)
                .withLineWidth(PATH_THICKNESS))
    }

    /**
     * Fills the regions described by the list of positions
     */
    private fun drawRegion(corners: List<LatLng>) {
        if (corners.isEmpty()) return

        fillManager.deleteAll()
        val fillOption = FillOptions()
                .withLatLngs(listOf(corners))
                .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                .withFillOpacity(REGION_FILL_OPACITY)
        fillManager.create(fillOption)

        //Draw the borders
        val linePoints = arrayListOf<LatLng>().apply {
            addAll(corners)
            add(corners.first())
        }
        val lineOptions = LineOptions()
                .withLatLngs(linePoints)
                .withLineColor(ColorUtils.colorToRgbaString(Color.LTGRAY))
        lineManager.deleteAll()
        lineManager.create(lineOptions)
    }

    /**
     * Draws a pinpoint on the map at the given position
     */
    private fun drawPinpoint(corners: List<LatLng>) {
        if (movingWaypoint) return

        circleManager.deleteAll()

        corners.forEach {
            val circleOptions = CircleOptions()
                    .withLatLng(it)
                    .withDraggable(true)
            circleManager.create(circleOptions)
        }
    }

}