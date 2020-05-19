package ch.epfl.sdp.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils
import kotlin.math.*

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
                onVertexMoved.forEach { reset = reset || !it(previousLocation, annotation.latLng) }
                previousLocation = annotation.latLng
            }

            override fun onAnnotationDragFinished(annotation: Circle?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        nbVertices = 0
        fillManager.deleteAll()
        circleManager.deleteAll()
        fillManager.onDestroy()
        circleManager.onDestroy()
    }

    override fun getUpperLayer(): String {
        return circleManager.layerId
    }

    override fun paint(vertices: List<LatLng>) {
        if (vertices.size != nbVertices || reset) {
            drawPinpoint(vertices)
            nbVertices = vertices.size
        }
        drawRegion(vertices)
        reset = false
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
            } else {
                fillArea.latLngs = listOf(polygonCircle)
                fillManager.update(fillArea)
            }
        } else {
            fillManager.deleteAll()
            reset = true
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
        val numberOfPoints = floor((360 / degreesBetweenPoints).toDouble()).toInt()
        val distRadians = radius / 6371000.0 // earth radius in meters
        val centerLatRadians = location.latitude * Math.PI / 180
        val centerLonRadians = location.longitude * Math.PI / 180
        val vertices = arrayListOf<LatLng>() //array to hold all the points
        for (index in 0 until numberOfPoints) {
            val degrees = (index * degreesBetweenPoints).toDouble()
            val degreeRadians = degrees * Math.PI / 180
            val pointLatRadians = asin(sin(centerLatRadians) * cos(distRadians) + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians))
            val pointLonRadians = centerLonRadians + atan2(sin(degreeRadians) * sin(distRadians) * cos(centerLatRadians),
                    cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians))
            val pointLat = pointLatRadians * 180 / Math.PI
            val pointLon = pointLonRadians * 180 / Math.PI
            val point = LatLng(pointLat, pointLon)
            vertices.add(point)
        }
        return vertices
    }
}