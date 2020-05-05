package ch.epfl.sdp.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

class MapboxCirclePainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) :
        MapboxSearchAreaPainter() {

    companion object {
        private const val REGION_FILL_OPACITY: Float = 0.5F
    }

    private var fillManager = FillManager(mapView, mapboxMap, style)
    private var circleManager = CircleManager(mapView, mapboxMap, style)

    private lateinit var fillArea: Fill

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

    override fun getUpperLayer(): String {
        return circleManager.layerId
    }

    override fun unMount() {
        super.unMount()
        nbVertices = 0
        fillManager.deleteAll()
        circleManager.deleteAll()
    }

    override fun paint(vertices: List<LatLng>) {
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
        if (vertices.size >= 2) {
            val polygonCircle = polygonCircleForCoordinate(vertices[0], vertices[0].distanceTo(vertices[1]))
            if (!::fillArea.isInitialized || reset) {
                fillManager.deleteAll()
                val fillOption = FillOptions()
                    .withLatLngs(listOf(polygonCircle))
                    .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                    .withFillOpacity(REGION_FILL_OPACITY)
                fillArea = fillManager.create(fillOption)
                reset = false
            } else {
                fillArea.latLngs = listOf(polygonCircle)
                fillManager.update(fillArea)
            }
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

    private fun polygonCircleForCoordinate(location: LatLng, radius: Double): ArrayList<LatLng> {
        val degreesBetweenPoints = 8 //45 sides
        val numberOfPoints = Math.floor((360 / degreesBetweenPoints).toDouble()).toInt()
        val distRadians = radius / 6371000.0 // earth radius in meters
        val centerLatRadians = location.latitude * Math.PI / 180
        val centerLonRadians = location.longitude * Math.PI / 180
        val vertices = arrayListOf<LatLng>() //array to hold all the points
        for (index in 0 until numberOfPoints) {
            val degrees = (index * degreesBetweenPoints).toDouble()
            val degreeRadians = degrees * Math.PI / 180
            val pointLatRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians))
            val pointLonRadians = centerLonRadians + Math.atan2(Math.sin(degreeRadians) * Math.sin(distRadians) * Math.cos(centerLatRadians),
                    Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians))
            val pointLat = pointLatRadians * 180 / Math.PI
            val pointLon = pointLonRadians * 180 / Math.PI
            val point = LatLng(pointLat, pointLon)
            vertices.add(point)
        }
        return vertices
    }
}