package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
        val tested = CountDownLatch(1)
        val expectedData = MutableLiveData(setOf(UserData(uuid = DUMMY_USER_ID)))
        val expectedRole = Role.OPERATOR
        lateinit var actualRole: Role
        lateinit var actualGroupId: String

        val dao = object : EmptyMockUserDao() {
            override fun getUsersOfGroupWithRole(groupId: String, role: Role): MutableLiveData<Set<UserData>> {
                tested.countDown()
                actualRole = role
                actualGroupId = groupId
                return expectedData
            }
        }
        UserRepository.daoProvider = { dao }

        val repo = UserRepository()
        assertThat(repo.getOperatorsOfSearchGroup(DUMMY_GROUP_ID), equalTo(expectedData))

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualRole, equalTo(expectedRole))
        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun getRescuersOfSearchGroupCallsGetUsersOfGroupWithRoleFromDao() {
        val tested = CountDownLatch(1)
        val expectedData = MutableLiveData(setOf(UserData(uuid = DUMMY_USER_ID)))
        val expectedRole = Role.RESCUER
        lateinit var actualRole: Role
        lateinit var actualGroupId: String

        val dao = object : EmptyMockUserDao() {
            override fun getUsersOfGroupWithRole(groupId: String, role: Role): MutableLiveData<Set<UserData>> {
                tested.countDown()
                actualGroupId = groupId
                actualRole = role
                return expectedData
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        assertThat(repo.getRescuersOfSearchGroup(DUMMY_GROUP_ID), equalTo(expectedData))

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualRole, equalTo(expectedRole))
        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun removeUserFromSearchGroupCallsRemoveUserFromSearchGroupWithCorrectParameters() {
        val tested = CountDownLatch(1)
        lateinit var actualGroupId: String
        lateinit var actualUserId: String

        val dao = object : EmptyMockUserDao() {
            override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {
                tested.countDown()
                actualGroupId = searchGroupId
                actualUserId = userId
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.removeUserFromSearchGroup(DUMMY_GROUP_ID, DUMMY_USER_ID)

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualUserId, equalTo(DUMMY_USER_ID))
        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun removeAllUserFromSearchGroupCallsRemoveAllUserFromSearchGroupWithCorrectParameters() {
        val tested = CountDownLatch(1)
        lateinit var actualGroupId: String

        val dao = object : EmptyMockUserDao() {
            override fun removeAllUserOfSearchGroup(searchGroupId: String) {
                tested.countDown()
                actualGroupId = searchGroupId
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.removeAllUserOfSearchGroup(DUMMY_GROUP_ID)

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
    }

//    override fun addUserToSearchGroup(searchGroupId: String, user: UserData)

    @Test
    fun addUserToSearchGroupCallsAddUserToSearchGroupWithCorrectParameters() {
        val tested = CountDownLatch(1)
        val expectedUserData = UserData(DUMMY_EMAIL, DUMMY_USER_ID, Role.RESCUER)
        lateinit var actualGroupId: String
        lateinit var actualUser: UserData

        val dao = object : EmptyMockUserDao() {
            override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {
                tested.countDown()
                actualGroupId = searchGroupId
                actualUser = user
            }
        }
        UserRepository.daoProvider = { dao }
        val repo = UserRepository()

        repo.addUserToSearchGroup(DUMMY_GROUP_ID, expectedUserData)

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(DUMMY_GROUP_ID))
        assertThat(actualUser, equalTo(expectedUserData))
    }
}