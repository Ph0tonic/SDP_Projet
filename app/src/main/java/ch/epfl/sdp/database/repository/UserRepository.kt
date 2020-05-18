package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseUserDao
import ch.epfl.sdp.database.dao.UserDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData

class UserRepository : IUserRepository {
    companion object {
        val DEFAULT_DAO = { FirebaseUserDao() }

        // Change this for dependency injection
        var daoProvider: () -> UserDao = DEFAULT_DAO
    }

    val dao: UserDao = daoProvider()

    override fun getOperatorsOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
        return dao.getUsersOfGroupWithRole(searchGroupId, Role.OPERATOR)
    }

    override fun getRescuersOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
        return dao.getUsersOfGroupWithRole(searchGroupId, Role.RESCUER)
    }

    override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {
        dao.removeUserFromSearchGroup(searchGroupId, userId)
    }

    override fun removeAllUserOfSearchGroup(searchGroupId: String) {
        dao.removeAllUserOfSearchGroup(searchGroupId)
    }

    override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {
        dao.addUserToSearchGroup(searchGroupId, user)
    }
}