package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.FirebaseMarkersDao
import com.mapbox.mapboxsdk.geometry.LatLng

class GroupMarkersDataRepository(
        private val markersDao: FirebaseMarkersDao
) {
    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, LatLng>>{return markersDao.getMarkersOfSearchGroup(groupId)}
}