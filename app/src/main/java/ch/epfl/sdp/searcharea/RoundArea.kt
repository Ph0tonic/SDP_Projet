package ch.epfl.sdp.searcharea

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng

class RoundArea : SearchArea {

    companion object {
        const val PROPS_KEY_RADIUS = "RADIUS_PROPS"
    }

    private val center: MutableLiveData<MutableList<LatLng>> = MutableLiveData(mutableListOf())
    private val props: MutableLiveData<MutableMap<String, Double>> = MutableLiveData(mutableMapOf())

    fun setCenter(center: LatLng) {
        this.center.postValue(mutableListOf(center))
    }

    fun setRadius(radius: Double) {
        props.postValue(mutableMapOf(Pair(PROPS_KEY_RADIUS, radius)))
    }

    override fun isComplete(): Boolean {
        return props.value?.containsKey(PROPS_KEY_RADIUS)!! &&
                center.value?.size == 1
    }

    override fun getLatLng(): MutableLiveData<MutableList<LatLng>> {
        return center
    }

    override fun getAdditionalProps(): MutableLiveData<MutableMap<String, Double>> {
        return props
    }

    override fun reset() {
        props.postValue(mutableMapOf())
        center.postValue(mutableListOf())
    }
}