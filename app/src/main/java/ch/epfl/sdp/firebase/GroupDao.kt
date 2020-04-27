package ch.epfl.sdp.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface GroupDao {
    fun getGroups() : MutableLiveData<List<SearchGroup>>
}