package ch.epfl.sdp.map

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ch.epfl.sdp.searcharea.QuadrilateralArea
import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

class MapBoxQuadrilateralBuilder : MapBoxSearchAreaBuilder {

    override fun onMapClicked(position: LatLng) {
        if (!searchArea.isComplete()) searchArea.addAngle(position)
    }

    override fun onMapLongClicked(position: LatLng) {
        //No action yet
    }

    companion object {
        private const val PATH_THICKNESS: Float = 2F
        private const val REGION_FILL_OPACITY: Float = 0.5F
    }

    private lateinit var circleManager: CircleManager
    private lateinit var lineManager: LineManager
    private lateinit var fillManager: FillManager

    private lateinit var fillArea: Fill
    private lateinit var lineArea: Line

    private var movingWaypoint: Boolean = false
    private var reseted: Boolean = false
    val searchArea = QuadrilateralArea()

    private lateinit var observer: Observer<MutableList<LatLng>>

    override fun mount(lifecycleOwner: LifecycleOwner, mapView: MapView, mapboxMap: MapboxMap, style: Style) {
        fillManager = FillManager(mapView, mapboxMap, style)
        lineManager = LineManager(mapView, mapboxMap, style)
        circleManager = CircleManager(mapView, mapboxMap, style)

        observer = Observer {
//            drawPath(it)
            drawRegion(it)
            drawPinpoint(it)
        }
        searchArea.getLatLng().observe(lifecycleOwner, this.observer)

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

    override fun unMount() {
        circleManager.deleteAll()
        lineManager.deleteAll()
        fillManager.deleteAll()

        searchArea.getLatLng().removeObserver(observer)
    }

    override fun resetSearchArea() {
        searchArea.reset()

        circleManager.deleteAll()
        lineManager.deleteAll()
        fillManager.deleteAll()
        reseted = true
    }

    override fun searchArea(): SearchArea {
        return searchArea
    }

    /**
     * Fills the regions described by the list of positions
     */
    private fun drawRegion(corners: List<LatLng>) {
        if (corners.isEmpty()) return

        if (!::fillArea.isInitialized || reseted) {
            fillManager.deleteAll()
            val fillOption = FillOptions()
                    .withLatLngs(listOf(corners))
                    .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                    .withFillOpacity(REGION_FILL_OPACITY)
            fillArea = fillManager.create(fillOption)
            reseted = false
        } else {
            fillArea.latLngs = listOf(corners)
            fillManager.update(fillArea)
        }

//        //Draw the borders
//        val linePoints = arrayListOf<LatLng>().apply {
//            addAll(corners)
//            add(corners.first())
//        }
//        if (!::lineArea.isInitialized || reseted) {
//            lineManager.deleteAll()
//            val lineOptions = LineOptions()
//                    .withLatLngs(linePoints)
//                    .withLineWidth(PATH_THICKNESS)
//                    .withLineColor(ColorUtils.colorToRgbaString(Color.LTGRAY))
//            lineArea = lineManager.create(lineOptions)
//            reseted = false
//        } else {
//            lineArea.latLngs = linePoints
//            lineManager.update(lineArea)
//        }
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