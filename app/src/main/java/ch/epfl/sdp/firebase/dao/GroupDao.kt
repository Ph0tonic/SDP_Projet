package ch.epfl.sdp.firebase.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.data.SearchGroup
import ch.epfl.sdp.firebase.data.User

interface GroupDao {
    fun getGroups() : MutableLiveData<List<SearchGroup>>
}