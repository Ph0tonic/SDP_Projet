package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.GroupDao
import ch.epfl.sdp.firebase.dao.OnGroupChangedListener
import ch.epfl.sdp.firebase.data.SearchGroup

class SearchGroupDataRepository (
        private val groupDao: GroupDao) {
    fun getGroups():MutableLiveData<List<SearchGroup>>{return groupDao.getGroups()}
    fun addOnGroupChangedListener(OnGroupChangedListener: OnGroupChangedListener, groupId: String){
        groupDao.addOnGroupChangedListener(OnGroupChangedListener, groupId)
    }
    fun removeGroupListeners(groupId: String){groupDao.removeGroupListeners(groupId)}
}