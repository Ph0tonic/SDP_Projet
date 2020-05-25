package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.UserDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {

    companion object {
        private const val ASYNC_CALL_TIMEOUT = 5L
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_USER_ID = "Dummy_marker_id"
        private const val DUMMY_EMAIL = "dummy@gm.co"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getOperatorsOfSearchGroupCallsGetUsersOfGroupWithRoleFromDao() {
        val expectedData = MutableLiveData(setOf(UserData(uuid = DUMMY_USER_ID)))
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedRole = Role.OPERATOR

        val dao = Mockito.mock(UserDao::class.java)
        Mockito.`when`(dao.getUsersOfGroupWithRole(expectedGroupId, expectedRole)).thenReturn(expectedData)

        UserRepository.daoProvider = { dao }
        val repo = UserRepository()
        assertThat(repo.getOperatorsOfSearchGroup(DUMMY_GROUP_ID), equalTo(expectedData as LiveData<Set<UserData>>))

        Mockito.verify(dao, Mockito.times(1)).getUsersOfGroupWithRole(expectedGroupId, expectedRole)
    }

    @Test
    fun getRescuersOfSearchGroupCallsGetUsersOfGroupWithRoleFromDao() {
        val expectedData = MutableLiveData(setOf(UserData(uuid = DUMMY_USER_ID)))
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedRole = Role.RESCUER

        val dao = Mockito.mock(UserDao::class.java)
        Mockito.`when`(dao.getUsersOfGroupWithRole(expectedGroupId, expectedRole)).thenReturn(expectedData)

        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        assertThat(repo.getRescuersOfSearchGroup(DUMMY_GROUP_ID), equalTo(expectedData as LiveData<Set<UserData>>))
        Mockito.verify(dao, Mockito.times(1)).getUsersOfGroupWithRole(expectedGroupId, expectedRole)
    }

    @Test
    fun removeUserFromSearchGroupCallsRemoveUserFromSearchGroupWithCorrectParameters() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedUserId = DUMMY_USER_ID

        val dao = Mockito.mock(UserDao::class.java)

        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.removeUserFromSearchGroup(expectedGroupId, expectedUserId)
        Mockito.verify(dao, Mockito.times(1)).removeUserFromSearchGroup(expectedGroupId, expectedUserId)
    }

    @Test
    fun removeAllUserFromSearchGroupCallsRemoveAllUserFromSearchGroupWithCorrectParameters() {
        val expectedGroupId = DUMMY_GROUP_ID

        val dao = Mockito.mock(UserDao::class.java)

        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.removeAllUserOfSearchGroup(expectedGroupId)
        Mockito.verify(dao, Mockito.times(1)).removeAllUserOfSearchGroup(expectedGroupId)
    }

    @Test
    fun addUserToSearchGroupCallsAddUserToSearchGroupWithCorrectParameters() {
        val expectedUserData = UserData(DUMMY_EMAIL, DUMMY_USER_ID, Role.RESCUER)
        val expectedGroupId = DUMMY_GROUP_ID

        val dao = Mockito.mock(UserDao::class.java)

        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.addUserToSearchGroup(expectedGroupId, expectedUserData)
        Mockito.verify(dao, Mockito.times(1)).addUserToSearchGroup(expectedGroupId, expectedUserData)
    }

    @Test
    fun getGroupIdsOfUserByEmailCallsGetGroupIdsOfUserByEmailWithCorrectParameters() {
        val expectedData = MutableLiveData(setOf(DUMMY_GROUP_ID))
        val expectedEmail = DUMMY_EMAIL

        val dao = Mockito.mock(UserDao::class.java)
        Mockito.`when`(dao.getGroupIdsOfUserByEmail(expectedEmail)).thenReturn(expectedData)

        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        assertThat(repo.getGroupIdsOfUserByEmail(expectedEmail), equalTo(expectedData as LiveData<Set<String>>))
        Mockito.verify(dao, Mockito.times(1)).getGroupIdsOfUserByEmail(expectedEmail)
    }
}