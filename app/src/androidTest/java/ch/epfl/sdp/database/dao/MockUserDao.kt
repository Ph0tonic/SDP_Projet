package ch.epfl.sdp.database.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData

class MockUserDao : UserDao {

    override fun getUsersOfGroupWithRole(groupId: String, role: Role): LiveData<Set<UserData>> {
        return MutableLiveData(setOf())
    }

    override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {
        TODO("Not yet implemented")
    }

    override fun removeAllUserOfSearchGroup(searchGroupId: String) {
        TODO("Not yet implemented")
    }

    override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {
        TODO("Not yet implemented")
    }

    override fun getGroupIdsOfUserByEmail(email: String): LiveData<Map<String, Role>> {
        return MutableLiveData(mapOf())
    }
}