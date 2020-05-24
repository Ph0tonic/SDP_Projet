package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.LiveData
import ch.epfl.sdp.database.data.MarkerData
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import com.mapbox.mapboxsdk.geometry.LatLng

class MarkerDataManager {

    val markerRepository = MarkerRepositoryProvider.provide()

    fun getMarkersOfSearchGroup(groupId: String): LiveData<Set<MarkerData>> {
        return markerRepository.getMarkersOfSearchGroup(groupId)
    }

    fun addMarkerForSearchGroup(groupId: String, position: LatLng) {
        markerRepository.addMarkerForSearchGroup(groupId, MarkerData(position))
    }

    fun removeMarkerForSearchGroup(groupId: String, markerId: String) {
        markerRepository.removeMarkerOfSearchGroup(groupId, markerId)
    }

    fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
        markerRepository.removeAllMarkersOfSearchGroup(searchGroupId)
    }
}