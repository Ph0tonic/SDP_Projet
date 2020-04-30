package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData

interface MarkerDao {
    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>>
    fun addMarker(groupId: String, markerData: MarkerData)
    fun removeMarker(groupId: String, markerId: String)
}
