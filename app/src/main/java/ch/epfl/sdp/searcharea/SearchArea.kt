package ch.epfl.sdp.searcharea

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng

interface SearchArea {

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun isComplete(): Boolean
    fun getLatLng(): MutableLiveData<MutableList<LatLng>>
    fun getAdditionalProps(): MutableLiveData<MutableMap<String,Double>>
    fun reset()
}
