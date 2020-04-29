package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.FirebaseMarkersDao
import ch.epfl.sdp.firebase.dao.MarkersDao
import com.mapbox.mapboxsdk.geometry.LatLng

class GroupMarkersDataRepository {

    companion object {
        val DEFAULT_DAO = { FirebaseMarkersDao() }

        // Change this for dependency injection
        var daoProvider: () -> MarkersDao = DEFAULT_DAO
    }

    val dao: MarkersDao = daoProvider()

    fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, LatLng>> {
        return dao.getMarkersOfSearchGroup(groupId)
    }
}