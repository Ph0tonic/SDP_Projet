package ch.epfl.sdp.map

import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlin.math.*
/*
class CircleAreaPainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) :
        OldSearchAreaPainter(mapView, mapboxMap, style) {

    override fun paint(vertices: List<LatLng>) {
        require(vertices.size <= 2) {"Circle has more than two points"}
        if(vertices.size == 2) {
            val polygonCircle = polygonCircleForCoordinate(vertices[0], vertices[0].distanceTo(vertices[1]))
            controlPaint(vertices, polygonCircle)
        }else{
            controlPaint(vertices,vertices)
        }
    }

    override fun getUpperLayer(): String {
        TODO("Not yet implemented")
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

 */