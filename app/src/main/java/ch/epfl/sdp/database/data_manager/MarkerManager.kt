package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseMarkersDao
import ch.epfl.sdp.database.dao.MarkerDao
import ch.epfl.sdp.database.data.MarkerData
import ch.epfl.sdp.database.repository.MarkerRepository
import com.mapbox.mapboxsdk.geometry.LatLng

class MarkerManager {

    val markerRepository = MarkerRepository()

    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        return markerRepository.getMarkersOfSearchGroup(groupId)
    }

    fun addMarkerForSearchGroup(groupId: String, position: LatLng) {
        markerRepository.addMarkerForSearchGroup(groupId, MarkerData(position))
    }

    fun removeMarkerForSearchGroup(groupId: String, markerId: String) {
        markerRepository.removeMarkerForSearchGroup(groupId, markerId)
    }

    fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
        markerRepository.removeAllMarkersOfSearchGroup(searchGroupId)
    }
}