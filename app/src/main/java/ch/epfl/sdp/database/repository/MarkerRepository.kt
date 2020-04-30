package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseMarkersDao
import ch.epfl.sdp.database.dao.MarkerDao
import ch.epfl.sdp.database.data.MarkerData
import com.mapbox.mapboxsdk.geometry.LatLng

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

    fun addMarkerForSearchGroup(groupId: String, position: LatLng) {
        dao.addMarker(groupId, MarkerData(position))
    }

    fun removeMarkerForSearchGroup(groupId: String, markerId: String) {
        dao.removeMarker(groupId, markerId)
    }
}