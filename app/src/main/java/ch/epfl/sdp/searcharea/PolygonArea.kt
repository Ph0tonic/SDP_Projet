package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonArea : SearchArea {

    private val angles = mutableListOf<LatLng>()

    fun getNbAngles(): Int {
        return angles.size
    }

    fun addAngle(angle: LatLng) {
        require(angles.size < 4) { "Max number of angles reached" }
        angles.add(angle)
    }

    override fun isComplete(): Boolean {
        return angles.size >= 3
    }

    override fun getLatLng(): List<LatLng> {
        return angles
    }
}