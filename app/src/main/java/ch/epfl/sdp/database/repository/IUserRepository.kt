package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData

interface IUserRepository {
    fun getOperatorsOfSearchGroup(searchGroupId: String): LiveData<Set<UserData>>
    fun getRescuersOfSearchGroup(searchGroupId: String): LiveData<Set<UserData>>
    fun removeUserFromSearchGroup(searchGroupId: String, userId: String)
    fun removeAllUserOfSearchGroup(searchGroupId: String)
    fun addUserToSearchGroup(searchGroupId: String, user: UserData)
    fun getGroupIdsOfUserByEmail(email: String): LiveData<Set<String>>
}
