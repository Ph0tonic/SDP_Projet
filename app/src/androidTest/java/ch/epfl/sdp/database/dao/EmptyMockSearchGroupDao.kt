package ch.epfl.sdp.database.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData

open class EmptyMockSearchGroupDao : SearchGroupDao {
    override fun getGroups(): LiveData<List<SearchGroupData>> {
        return MutableLiveData()
    }

    override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
        return MutableLiveData()
    }

    override fun createGroup(searchGroupData: SearchGroupData) {}
    override fun updateGroup(searchGroupData: SearchGroupData) {}
    override fun removeSearchGroup(searchGroupId: String) {}
}