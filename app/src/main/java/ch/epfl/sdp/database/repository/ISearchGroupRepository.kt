package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData

interface ISearchGroupRepository {
    fun getAllGroups(): LiveData<List<SearchGroupData>>
    fun getGroupById(groupId: String): LiveData<SearchGroupData>
    fun createGroup(searchGroupData: SearchGroupData) : String
    fun updateGroup(searchGroupData: SearchGroupData)
    fun removeSearchGroup(searchGroupId: String)
}
