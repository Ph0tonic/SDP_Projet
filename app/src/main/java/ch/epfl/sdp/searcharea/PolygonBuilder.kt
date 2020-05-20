package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 3
    override val sizeUpperBound: Int? = null
    override val shapeName: String = "Polygon"
    override fun buildGivenIsComplete(): PolygonArea = PolygonArea(vertices)
    override fun getShapeVerticesGivenComplete(): List<LatLng> = vertices
}