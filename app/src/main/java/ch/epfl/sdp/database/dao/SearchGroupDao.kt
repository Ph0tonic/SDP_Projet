package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData

interface SearchGroupDao {
    fun getGroups() : MutableLiveData<List<SearchGroupData>>
    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData>
    fun createGroup(searchGroupData: SearchGroupData)
    fun updateGroup(searchGroupData: SearchGroupData)
    fun getOperatorsOfGroup(groupId: String): MutableLiveData<Set<UserData>>
    fun getRescuersOfGroup(groupId: String): MutableLiveData<Set<UserData>>
}