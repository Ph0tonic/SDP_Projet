package ch.epfl.sdp.map

import android.graphics.Color
import ch.epfl.sdp.searcharea.SearchAreaBuilder
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

class SearchAreaPainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) : MapboxPainter {

    companion object {
        private const val REGION_FILL_OPACITY: Float = 0.5F
    }

    //val onVertexMoved = mutableListOf<(old: LatLng, new: LatLng) -> Unit>()

    private var fillManager: FillManager = FillManager(mapView, mapboxMap, style)
    private var circleManager: CircleManager = CircleManager(mapView, mapboxMap, style)

    private lateinit var fillArea: Fill

    private var reset: Boolean = false
    private var isDisplayDrawn = false

    private var nbVertices = 0

    lateinit var searchAreaBuilder: SearchAreaBuilder

    private val dragListener = object : OnCircleDragListener {
        lateinit var previousLocation: LatLng
        override fun onAnnotationDragStarted(annotation: Circle) {
            previousLocation = annotation.latLng
        }

        override fun onAnnotationDrag(annotation: Circle) {
            val currentLocation = annotation.latLng
            //onVertexMoved.forEach { it(previousLocation, annotation.latLng) }
            searchAreaBuilder.moveVertex(previousLocation, currentLocation)
            previousLocation = currentLocation
        }

        override fun onAnnotationDragFinished(annotation: Circle?) {}
    }

    init {
        circleManager.addDragListener(dragListener)
    }

    fun getUpperLayer(): String {
        return circleManager.layerId
    }

    override fun onDestroy() {
        //onVertexMoved.clear()
        nbVertices = 0
        fillManager.deleteAll()
        circleManager.deleteAll()
        fillManager.onDestroy()
        circleManager.onDestroy()
    }

    fun paint(searchArea: SearchAreaBuilder) {
        val (controlPoints, displayPoints) = searchArea.toControlDisplayPair()
        controlPaint(controlPoints, displayPoints)
    }

    private fun controlPaint(controlPoints: List<LatLng>, displayPoints: List<LatLng>?) {
        if (controlPoints.size != nbVertices || reset) {
            drawPinpoint(controlPoints)
            nbVertices = controlPoints.size
        }
        displayPoints?.let { drawRegion(it) } ?: unDrawRegion()
        reset = false
    }

    private fun unDrawRegion() {
        if (isDisplayDrawn) {
            drawRegion(listOf())
            isDisplayDrawn = false
        }
    }

    /**
     * Fills the regions described by the list of positions
     */
    private fun drawRegion(vertices: List<LatLng>) {
        if (!::fillArea.isInitialized || reset) {
            fillManager.deleteAll()
            val fillOption = FillOptions()
                    .withLatLngs(listOf(vertices))
                    .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                    .withFillOpacity(REGION_FILL_OPACITY)
            fillArea = fillManager.create(fillOption)
        } else {
            fillArea.latLngs = listOf(vertices)
            fillManager.update(fillArea)
        }
        isDisplayDrawn = true
    }

    /**
     * Draws a pinpoint on the map at the given position
     */
    private fun drawPinpoint(vertices: List<LatLng>) {
        circleManager.deleteAll()

        vertices.forEach {
            val circleOptions = CircleOptions()
                    .withLatLng(it)
                    .withDraggable(true)
            circleManager.create(circleOptions)
        }
    }
}