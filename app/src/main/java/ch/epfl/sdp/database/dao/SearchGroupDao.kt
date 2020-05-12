package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData

interface SearchGroupDao {
    fun getGroups() : MutableLiveData<List<SearchGroupData>>
    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData>
    fun createGroup(searchGroupData: SearchGroupData)
    fun updateGroup(searchGroupData: SearchGroupData)
}