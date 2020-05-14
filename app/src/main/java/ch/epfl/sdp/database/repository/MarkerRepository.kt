package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseMarkersDao
import ch.epfl.sdp.database.dao.MarkerDao
import ch.epfl.sdp.database.data.MarkerData

class MarkerRepository {

    companion object {
        val DEFAULT_DAO = { FirebaseMarkersDao() }

        // Change this for dependency injection
        var daoProvider: () -> MarkerDao = DEFAULT_DAO
    }

    private val dao: MarkerDao = daoProvider()

    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        return dao.getMarkersOfSearchGroup(groupId)
    }

    fun addMarkerForSearchGroup(groupId: String, marker: MarkerData) {
        dao.addMarker(groupId, marker)
    }

    fun removeMarkerForSearchGroup(groupId: String, markerId: String) {
        dao.removeMarker(groupId, markerId)
    }

    fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
        dao.removeAllMarkersOfSearchGroup(searchGroupId)
    }
}