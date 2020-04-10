package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class QuadrilateralArea : SearchArea {

    private val angles = mutableListOf<LatLng>()

    fun addAngle(angle: LatLng) {
        require(angles.size < 4) { "Max number of angles reached" }
        angles.add(angle)
    }

    override fun isComplete(): Boolean {
        return angles.size == 4
    }

    override fun getLatLng(): List<LatLng> {
        return angles
    }
}