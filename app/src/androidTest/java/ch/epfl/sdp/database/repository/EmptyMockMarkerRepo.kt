package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData
import ch.epfl.sdp.database.repository.IMarkerRepository

open class EmptyMockMarkerRepo : IMarkerRepository {
    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        return MutableLiveData()
    }

    override fun addMarkerForSearchGroup(groupId: String, marker: MarkerData) {}
    override fun removeMarkerOfSearchGroup(groupId: String, markerId: String) {}
    override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
}