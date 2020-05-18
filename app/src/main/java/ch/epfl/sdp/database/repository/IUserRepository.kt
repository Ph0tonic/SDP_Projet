package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.UserData

interface IUserRepository {
    fun getOperatorsOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>>
    fun getRescuersOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>>
    fun removeUserFromSearchGroup(searchGroupId: String, userId: String)
    fun removeAllUserOfSearchGroup(searchGroupId: String)
    fun addUserToSearchGroup(searchGroupId: String, user: UserData)
}
