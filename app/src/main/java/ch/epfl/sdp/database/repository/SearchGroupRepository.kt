package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseGroupDao
import ch.epfl.sdp.database.dao.GroupDao
import ch.epfl.sdp.database.data.SearchGroupData

class SearchGroupRepository {
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