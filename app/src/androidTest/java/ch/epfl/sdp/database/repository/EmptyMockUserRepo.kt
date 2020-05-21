package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.repository.IUserRepository

open class EmptyMockUserRepo: IUserRepository {
    override fun getOperatorsOfSearchGroup(searchGroupId: String): LiveData<Set<UserData>> {
        return MutableLiveData()
    }
    override fun getRescuersOfSearchGroup(searchGroupId: String): LiveData<Set<UserData>> {
        return MutableLiveData()
    }
    override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {}
    override fun removeAllUserOfSearchGroup(searchGroupId: String) {}
    override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {}
    override fun getGroupIdsOfUserByEmail(email: String): LiveData<Set<String>> {
        return MutableLiveData()
    }
}