package ch.epfl.sdp.firebase.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.data.SearchGroup

interface GroupDao {
    fun getGroups() : MutableLiveData<List<SearchGroup>>
    fun addOnGroupChangedListener(OnGroupChangedListener: OnGroupChangedListener, groupId: String)
    fun removeGroupListeners(groupId: String)
}