package ch.epfl.sdp.searcharea

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng

abstract class SearchArea {

    val latLngs: MutableLiveData<MutableList<LatLng>> = MutableLiveData(mutableListOf())
    val props: MutableLiveData<MutableMap<String, Double>> = MutableLiveData(mutableMapOf())

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    abstract fun isComplete(): Boolean
    abstract fun reset()

    fun getLatLng(): MutableLiveData<MutableList<LatLng>> {
        return latLngs
    }

    fun getAdditionalProps(): MutableLiveData<MutableMap<String, Double>> {
        return props
    }
}
