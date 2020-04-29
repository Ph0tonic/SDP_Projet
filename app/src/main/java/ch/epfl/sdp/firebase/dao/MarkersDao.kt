package ch.epfl.sdp.firebase.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.data.MarkerData
import com.mapbox.mapboxsdk.geometry.LatLng

interface MarkersDao {
    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>>
    fun addMarker(groupId: String, markerData: MarkerData)
    fun removeMarker(groupId: String, markerId: String)
}
