package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData

open class EmptyMockUserDao : UserDao {
    override fun getUsersOfGroupWithRole(groupId: String, role: Role): MutableLiveData<Set<UserData>> {
        return MutableLiveData()
    }

    override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {}
    override fun removeAllUserOfSearchGroup(searchGroupId: String) {}
    override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {}

}