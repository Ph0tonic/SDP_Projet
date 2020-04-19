package ch.epfl.sdp.searcharea

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonArea : SearchArea() {

    fun getNbAngles(): Int {
        return latLngs.value?.size!!
    }

    fun addAngle(angle: LatLng) {
        latLngs.value?.add(angle)
        latLngs.notifyObserver()
    }

    override fun isComplete(): Boolean {
        return latLngs.value?.size!! >= 3
    }

    override fun reset() {
        latLngs.postValue(mutableListOf())
    }
}