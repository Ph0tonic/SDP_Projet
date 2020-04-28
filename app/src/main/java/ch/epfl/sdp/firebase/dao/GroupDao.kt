package ch.epfl.sdp.firebase.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.data.SearchGroupData

interface GroupDao {
    fun getGroups() : MutableLiveData<List<SearchGroupData>>
    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData>
}