package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.repository.ISearchGroupRepository

open class EmptyMockSearchGroupRepo : ISearchGroupRepository {
    override fun getAllGroups(): MutableLiveData<List<SearchGroupData>> {
        return MutableLiveData()
    }

    override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
        return MutableLiveData()
    }

    override fun createGroup(searchGroupData: SearchGroupData) {}
    override fun updateGroup(searchGroupData: SearchGroupData) {}
    override fun removeSearchGroup(searchGroupId: String) {}
}