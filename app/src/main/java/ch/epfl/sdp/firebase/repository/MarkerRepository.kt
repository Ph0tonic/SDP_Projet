package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.FirebaseMarkersDao
import ch.epfl.sdp.firebase.dao.MarkersDao
import ch.epfl.sdp.firebase.data.MarkerData
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.geometry.LatLng

class MarkerRepository {

    companion object {
        val DEFAULT_DAO = { FirebaseMarkersDao() }

        // Change this for dependency injection
        var daoProvider: () -> MarkersDao = DEFAULT_DAO
    }

    private val dao: MarkersDao = daoProvider()

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