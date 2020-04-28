package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.GroupDao
import ch.epfl.sdp.firebase.data.SearchGroupData

class SearchGroupDataRepository (
        private val groupDao: GroupDao) {
    fun getGroups():MutableLiveData<List<SearchGroupData>>{return groupDao.getGroups()}
    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData>{return groupDao.getGroupById(groupId)}
}