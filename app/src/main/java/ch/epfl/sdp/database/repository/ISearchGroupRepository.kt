package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData

interface ISearchGroupRepository {
    fun getAllGroups(): MutableLiveData<List<SearchGroupData>>
    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData>
    fun createGroup(searchGroupData: SearchGroupData)
    fun updateGroup(searchGroupData: SearchGroupData)
    fun removeSearchGroup(searchGroupId: String)
}
