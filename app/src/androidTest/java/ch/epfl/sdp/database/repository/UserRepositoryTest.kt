package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.EmptyMockUserDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
        val called = CountDownLatch(1)
        val expectedData = MutableLiveData(setOf(UserData(uuid = DUMMY_USER_ID)))
        val expectedRole = Role.OPERATOR
        lateinit var actualRole: Role
        lateinit var actualGroupId: String

        val dao = object : EmptyMockUserDao() {
            override fun getUsersOfGroupWithRole(groupId: String, role: Role): MutableLiveData<Set<UserData>> {
                called.countDown()
                actualRole = role
                actualGroupId = groupId
                return expectedData
            }
        }
        UserRepository.daoProvider = { dao }

        val repo = UserRepository()
        assertThat(repo.getOperatorsOfSearchGroup(DUMMY_GROUP_ID), equalTo(expectedData as LiveData<Set<UserData>>))

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualRole, equalTo(expectedRole))
        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun getRescuersOfSearchGroupCallsGetUsersOfGroupWithRoleFromDao() {
        val called = CountDownLatch(1)
        val expectedData = MutableLiveData(setOf(UserData(uuid = DUMMY_USER_ID)))
        val expectedRole = Role.RESCUER
        lateinit var actualRole: Role
        lateinit var actualGroupId: String

        val dao = object : EmptyMockUserDao() {
            override fun getUsersOfGroupWithRole(groupId: String, role: Role): MutableLiveData<Set<UserData>> {
                called.countDown()
                actualGroupId = groupId
                actualRole = role
                return expectedData
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        assertThat(repo.getRescuersOfSearchGroup(DUMMY_GROUP_ID), equalTo(expectedData as LiveData<Set<UserData>>))

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualRole, equalTo(expectedRole))
        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun removeUserFromSearchGroupCallsRemoveUserFromSearchGroupWithCorrectParameters() {
        val called = CountDownLatch(1)
        lateinit var actualGroupId: String
        lateinit var actualUserId: String

        val dao = object : EmptyMockUserDao() {
            override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {
                called.countDown()
                actualGroupId = searchGroupId
                actualUserId = userId
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.removeUserFromSearchGroup(DUMMY_GROUP_ID, DUMMY_USER_ID)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualUserId, equalTo(DUMMY_USER_ID))
        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun removeAllUserFromSearchGroupCallsRemoveAllUserFromSearchGroupWithCorrectParameters() {
        val called = CountDownLatch(1)
        lateinit var actualGroupId: String

        val dao = object : EmptyMockUserDao() {
            override fun removeAllUserOfSearchGroup(searchGroupId: String) {
                called.countDown()
                actualGroupId = searchGroupId
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.removeAllUserOfSearchGroup(DUMMY_GROUP_ID)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun addUserToSearchGroupCallsAddUserToSearchGroupWithCorrectParameters() {
        val called = CountDownLatch(1)
        val expectedUserData = UserData(DUMMY_EMAIL, DUMMY_USER_ID, Role.RESCUER)
        lateinit var actualGroupId: String
        lateinit var actualUser: UserData

        val dao = object : EmptyMockUserDao() {
            override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {
                called.countDown()
                actualGroupId = searchGroupId
                actualUser = user
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.addUserToSearchGroup(DUMMY_GROUP_ID, expectedUserData)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
        assertThat(actualUser, equalTo(expectedUserData))
    }

    @Test
    fun getGroupIdsOfUserByEmailCallsGetGroupIdsOfUserByEmailWithCorrectParameters() {
        val called = CountDownLatch(1)
        val expectedIds = setOf(DUMMY_GROUP_ID)
        lateinit var actualEmail: String

        val dao = object : EmptyMockUserDao() {
            override fun getGroupIdsOfUserByEmail(email: String): LiveData<Set<String>> {
                called.countDown()
                actualEmail = email
                return MutableLiveData(expectedIds)
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        val ids = repo.getGroupIdsOfUserByEmail(DUMMY_EMAIL)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualEmail, equalTo(DUMMY_EMAIL))
        assertThat(ids.value, equalTo(expectedIds))
    }
}