package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseMarkersDao
import ch.epfl.sdp.database.dao.MarkerDao
import ch.epfl.sdp.database.data.MarkerData

class MarkerRepository : IMarkerRepository {

    companion object {
        val DEFAULT_DAO = { FirebaseMarkersDao() }

        // Change this for dependency injection
        var daoProvider: () -> MarkerDao = DEFAULT_DAO
    }

    private val dao: MarkerDao = daoProvider()

    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        return dao.getMarkersOfSearchGroup(groupId)
    }

    override fun addMarkerForSearchGroup(groupId: String, marker: MarkerData) {
        dao.addMarker(groupId, marker)
    }

    override fun removeMarkerOfSearchGroup(groupId: String, markerId: String) {
        dao.removeMarker(groupId, markerId)
    }

    override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
        dao.removeAllMarkersOfSearchGroup(searchGroupId)
    }
}