package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData

class EmptyMockMarkerDao : MarkerDao {
    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        return MutableLiveData()
    }

    override fun addMarker(groupId: String, markerData: MarkerData) {}
    override fun removeMarker(groupId: String, markerId: String) {}
    override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
}