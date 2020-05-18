package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData

interface IMarkerRepository {
    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>>
    fun addMarkerForSearchGroup(groupId: String, marker: MarkerData)
    fun removeMarkerOfSearchGroup(groupId: String, markerId: String)
    fun removeAllMarkersOfSearchGroup(searchGroupId: String)
}
