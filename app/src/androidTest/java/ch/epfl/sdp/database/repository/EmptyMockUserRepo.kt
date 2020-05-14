package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.repository.IUserRepository

open class EmptyMockUserRepo: IUserRepository {
    override fun getOperatorsOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
        return MutableLiveData()
    }
    override fun getRescuersOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
        return MutableLiveData()
    }
    override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {}
    override fun removeAllUserOfSearchGroup(searchGroupId: String) {}
    override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {}
}