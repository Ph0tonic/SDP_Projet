package ch.epfl.sdp.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

class MapboxQuadrilateralPainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) :
        MapboxSearchAreaPainter() {

    companion object {
        private const val PATH_THICKNESS: Float = 2F
        private const val REGION_FILL_OPACITY: Float = 0.5F
    }

    private var lineManager: LineManager = LineManager(mapView, mapboxMap, style)
    private var fillManager: FillManager = FillManager(mapView, mapboxMap, style)
    private var circleManager: CircleManager = CircleManager(mapView, mapboxMap, style)

    private lateinit var fillArea: Fill
    private lateinit var lineArea: Line

    private var reset: Boolean = false

    private var nbVertices = 0

    init {
        circleManager.addDragListener(object : OnCircleDragListener {
            lateinit var previousLocation: LatLng
            override fun onAnnotationDragStarted(annotation: Circle) {
                previousLocation = annotation.latLng
            }

            override fun onAnnotationDrag(annotation: Circle) {
                onMoveVertex.forEach { it(previousLocation, annotation.latLng) }
                previousLocation = annotation.latLng
            }

            override fun onAnnotationDragFinished(annotation: Circle?) {}
        })
    }

    override fun unMount() {
        super.unMount()
        nbVertices = 0
        lineManager.deleteAll()
        fillManager.deleteAll()
        circleManager.deleteAll()
    }

    override fun paint(vertices: List<LatLng>) {
        // drawPath(it)
        drawRegion(vertices)
        if (vertices.size != nbVertices) {
            drawPinpoint(vertices)
            nbVertices = vertices.size
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
            reset = false
        } else {
            fillArea.latLngs = listOf(vertices)
            fillManager.update(fillArea)
        }

//        //Draw the borders
//        val linePoints = arrayListOf<LatLng>().apply {
//            addAll(vertices)
//            add(vertices.first())
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