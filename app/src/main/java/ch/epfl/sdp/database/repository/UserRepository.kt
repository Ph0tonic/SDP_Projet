package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseUserDao
import ch.epfl.sdp.database.dao.UserDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData

class UserRepository {
    companion object {
        val DEFAULT_DAO = { FirebaseUserDao() }

        // Change this for dependency injection
        var daoProvider: () -> UserDao = DEFAULT_DAO
    }

    val dao: UserDao = daoProvider()

    fun getOperatorsOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
        return dao.getUsersOfGroupWithRole(searchGroupId, Role.OPERATOR)
    }

    fun getRescuersOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
        return dao.getUsersOfGroupWithRole(searchGroupId, Role.RESCUER)
    }

    fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {
        dao.removeUserFromSearchGroup(searchGroupId, userId)
    }

    fun removeAllUserOfSearchGroup(searchGroupId: String) {
        dao.removeAllUserOfSearchGroup(searchGroupId)
    }

    fun addUserToSearchGroup(searchGroupId: String, user: UserData) {
        dao.addUserToSearchGroup(searchGroupId, user)
    }
}