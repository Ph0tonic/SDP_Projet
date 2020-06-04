package ch.epfl.sdp.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

class MapboxSearchAreaPainter(mapView: MapView, mapboxMap: MapboxMap, style: Style, onLongClickConsumed: () -> Unit) : MapboxPainter {

    companion object {
        private const val REGION_FILL_OPACITY: Float = 0.5F
        private const val WAYPOINT_RADIUS: Float = 8f
    }

    val onVertexMoved = mutableListOf<(old: LatLng, new: LatLng) -> Unit>()

    private var fillManager: FillManager = FillManager(mapView, mapboxMap, style)
    private var circleManager: CircleManager = CircleManager(mapView, mapboxMap, style)

    private lateinit var fillArea: Fill

    private var reset: Boolean = false

    private var nbVertices = 0

    private val dragListener = object : OnCircleDragListener {
        lateinit var previousLocation: LatLng
        override fun onAnnotationDragStarted(annotation: Circle) {
            previousLocation = annotation.latLng
            onLongClickConsumed()
        }

        override fun onAnnotationDrag(annotation: Circle) {
            onVertexMoved.forEach { it(previousLocation, annotation.latLng) }
            previousLocation = annotation.latLng
        }

        override fun onAnnotationDragFinished(annotation: Circle?) {}
    }

    init {
        circleManager.addDragListener(dragListener)
        circleManager.addLongClickListener { onLongClickConsumed() }
    }

    fun getUpperLayer(): String {
        return circleManager.layerId
    }

    override fun onDestroy() {
        onVertexMoved.clear()
        nbVertices = 0
        fillManager.deleteAll()
        circleManager.deleteAll()
        fillManager.onDestroy()
        circleManager.onDestroy()
    }

    fun paint(pa: PaintableArea) {
        val controlVertices = pa.getControlVertices()
        val shapeVertices = pa.getShapeVertices()
        if (controlVertices.size != nbVertices || reset) {
            drawControlVertices(controlVertices)
            nbVertices = controlVertices.size
        }
        drawShape(shapeVertices ?: listOf())
        reset = false
    }

    /**
     * Draws a filled polygon described by the list of vertices
     * Those are the vertices that the user can not drag, and do not show up on the map,
     * Only the edges connecting them do
     */
    private fun drawShape(shapeOutline: List<LatLng>) {
        if (!::fillArea.isInitialized || reset) {
            fillManager.deleteAll()
            val fillOption = FillOptions()
                    .withLatLngs(listOf(shapeOutline))
                    .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                    .withFillOpacity(REGION_FILL_OPACITY)
            fillArea = fillManager.create(fillOption)
        } else {
            fillArea.latLngs = listOf(shapeOutline)
            fillManager.update(fillArea)
        }
    }

    /**
     * Draws the control vertices
     * Those are the vertices that the user can drag to modify the shape
     */
    private fun drawControlVertices(vertices: List<LatLng>) {
        circleManager.deleteAll()

        vertices.forEach {
            val circleOptions = CircleOptions()
                    .withCircleRadius(WAYPOINT_RADIUS)
                    .withLatLng(it)
                    .withDraggable(true)
            circleManager.create(circleOptions)
        }
    }
}