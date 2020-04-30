package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 3
    override val sizeUpperBound: Int? = null

    override fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("Quarilateral not complete")
        }
        return PolygonArea(vertices)
    }
}