package ch.epfl.sdp.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

open class PolygonAreaPainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) :
        SearchAreaPainter() {

    companion object {
        private const val REGION_FILL_OPACITY: Float = 0.5F
    }

    private var fillManager: FillManager = FillManager(mapView, mapboxMap, style)
    private var circleManager: CircleManager = CircleManager(mapView, mapboxMap, style)

    private lateinit var fillArea: Fill

    private var reset: Boolean = false

    private var nbVertices = 0

    private val dragListener = object : OnCircleDragListener {
        lateinit var previousLocation: LatLng
        override fun onAnnotationDragStarted(annotation: Circle) {
            previousLocation = annotation.latLng
        }

        override fun onAnnotationDrag(annotation: Circle) {
            onVertexMoved.forEach { it(previousLocation, annotation.latLng) }
            previousLocation = annotation.latLng
        }

        override fun onAnnotationDragFinished(annotation: Circle?) {}
    }

    init {
        circleManager.addDragListener(dragListener)
    }

    override fun getUpperLayer(): String {
        return circleManager.layerId
    }

    override fun onDestroy() {
        super.onDestroy()
        nbVertices = 0
        fillManager.deleteAll()
        circleManager.deleteAll()
        fillManager.onDestroy()
        circleManager.onDestroy()
    }

    override fun paint(vertices: List<LatLng>) {
        controlPaint(vertices, vertices)
    }

    protected open fun controlPaint(controlPoints: List<LatLng>, vertices: List<LatLng>) {
        if (controlPoints.size != nbVertices || reset) {
            drawPinpoint(controlPoints)
            nbVertices = controlPoints.size
        }
        drawRegion(vertices)
        reset = false
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