package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseGroupDao
import ch.epfl.sdp.database.dao.SearchGroupDao
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData

class SearchGroupRepository {
    companion object {
        val DEFAULT_DAO = { FirebaseGroupDao() }

        // Change this for dependency injection
        var daoProvider: () -> SearchGroupDao = DEFAULT_DAO
    }

    val dao: SearchGroupDao = daoProvider()

    fun getGroups(): MutableLiveData<List<SearchGroupData>> {
        return dao.getGroups()
    }

    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
        return dao.getGroupById(groupId)
    }

    fun createGroup(searchGroupData: SearchGroupData){
        return dao.createGroup(searchGroupData)
    }

    fun updateGroup(searchGroupData: SearchGroupData) {
        return dao.updateGroup(searchGroupData)
    }
}