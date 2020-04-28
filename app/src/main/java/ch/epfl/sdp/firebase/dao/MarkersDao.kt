package ch.epfl.sdp.firebase.dao

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng

interface MarkersDao {
    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, LatLng>>
}
