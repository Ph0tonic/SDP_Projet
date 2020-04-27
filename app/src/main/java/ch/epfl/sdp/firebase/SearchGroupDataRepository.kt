package ch.epfl.sdp.firebase

import androidx.lifecycle.MutableLiveData

class SearchGroupDataRepository (
        private val groupDao: GroupDao) {
    fun getGroups():MutableLiveData<List<SearchGroup>>{return groupDao.getGroups()}
}