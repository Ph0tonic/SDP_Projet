package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseGroupDao
import ch.epfl.sdp.database.dao.SearchGroupDao
import ch.epfl.sdp.database.data.SearchGroupData

class SearchGroupRepository : ISearchGroupRepository {
    companion object {
        val DEFAULT_DAO = { FirebaseGroupDao() }

        // Change this for dependency injection
        var daoProvider: () -> SearchGroupDao = DEFAULT_DAO
    }

    val dao: SearchGroupDao = daoProvider()

    override fun getAllGroups(): LiveData<List<SearchGroupData>> {
        return dao.getGroups()
    }

    override fun getGroupById(groupId: String): LiveData<SearchGroupData> {
        return dao.getGroupById(groupId)
    }

    override fun createGroup(searchGroupData: SearchGroupData) {
        return dao.createGroup(searchGroupData)
    }

    override fun updateGroup(searchGroupData: SearchGroupData) {
        return dao.updateGroup(searchGroupData)
    }

    override fun removeSearchGroup(searchGroupId: String) {
        dao.removeSearchGroup(searchGroupId)
    }
}