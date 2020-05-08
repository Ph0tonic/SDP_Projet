package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData

class MockGroupDao(data: List<SearchGroupData> = emptyList()) : SearchGroupDao {

    private val groups: MutableLiveData<List<SearchGroupData>> = MutableLiveData(data)

    override fun getGroups(): MutableLiveData<List<SearchGroupData>> {
        return groups
    }

    override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
        TODO("Not yet implemented")
    }
}