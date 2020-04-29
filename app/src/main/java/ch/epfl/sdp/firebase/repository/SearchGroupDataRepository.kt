package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.FirebaseGroupDao
import ch.epfl.sdp.firebase.dao.GroupDao
import ch.epfl.sdp.firebase.data.SearchGroupData

class SearchGroupDataRepository {
    companion object {
        val DEFAULT_DAO = { FirebaseGroupDao() }
        // Change this for dependency injection
        var daoProvider: () -> GroupDao = DEFAULT_DAO
    }

    val dao: GroupDao = daoProvider()

    fun getGroups(): MutableLiveData<List<SearchGroupData>> {
        return dao.getGroups()
    }

    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
        return dao.getGroupById(groupId)
    }
}