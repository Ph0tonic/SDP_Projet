package ch.epfl.sdp.map

import com.mapbox.mapboxsdk.geometry.LatLng

interface PaintableArea {
    fun getControlVertices(): List<LatLng>
    fun getShapeVertices(): List<LatLng>?
}