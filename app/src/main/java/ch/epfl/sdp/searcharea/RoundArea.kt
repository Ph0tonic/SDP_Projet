package ch.epfl.sdp.searcharea

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng

class RoundArea(private val center: LatLng, private val radius: Double) : SearchArea {

    private val angles: MutableLiveData<MutableList<LatLng>> = MutableLiveData(mutableListOf(center))

    init {

    }

    override fun isComplete(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLatLng(): MutableLiveData<MutableList<LatLng>> {
        return angles
    }
}