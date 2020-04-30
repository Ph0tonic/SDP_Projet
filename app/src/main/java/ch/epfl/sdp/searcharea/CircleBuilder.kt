package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class CircleBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 2
    override val sizeUpperBound: Int? = 2

    override fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("Circle not complete: Needs 2 points")
        }
        return CircleArea(vertices[0], vertices[1])
    }
}