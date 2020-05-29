package ch.epfl.sdp.database.dao

import androidx.lifecycle.LiveData
import ch.epfl.sdp.database.data.SearchGroupData

interface SearchGroupDao {
    fun getGroups(): LiveData<List<SearchGroupData>>
    fun getGroupById(groupId: String): LiveData<SearchGroupData>
    fun createGroup(searchGroupData: SearchGroupData): String
    fun updateGroup(searchGroupData: SearchGroupData)
    fun removeSearchGroup(searchGroupId: String)
}