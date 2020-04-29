package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData

interface GroupDao {
    fun getGroups() : MutableLiveData<List<SearchGroupData>>
    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData>
}