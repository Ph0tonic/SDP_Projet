package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.EmptyMockHeatmapRepo
import ch.epfl.sdp.database.repository.EmptyMockMarkerRepo
import ch.epfl.sdp.database.repository.EmptyMockSearchGroupRepo
import ch.epfl.sdp.database.repository.EmptyMockUserRepo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SearchGroupDataManagerTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_USER_ID = "Dummy_user_id"
        private const val ASYNC_CALL_TIMEOUT = 5L
        private const val DUMMY_EMAIL = "test@test.com"
        private val DUMMY_ROLE = Role.RESCUER
    }

    @Before
    fun setup() {
        SearchGroupRepositoryProvider.provide = { EmptyMockSearchGroupRepo() }
        UserRepositoryProvider.provide = { EmptyMockUserRepo() }
        MarkerRepositoryProvider.provide = { EmptyMockMarkerRepo() }
        HeatmapRepositoryProvider.provide = { EmptyMockHeatmapRepo() }
    }

    @Test
    fun deleteSearchGroupCallsRemoveOfSearchGroupWithCorrectGroupId() {
        val calledSearchGroupRepository = CountDownLatch(1)

        lateinit var actualGroupIdInSearchGroup: String

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun removeSearchGroup(searchGroupId: String) {
                actualGroupIdInSearchGroup = searchGroupId
                calledSearchGroupRepository.countDown()
            }
        }

        SearchGroupRepositoryProvider.provide = { searchGroupRepo }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledSearchGroupRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledSearchGroupRepository.count, equalTo(0L))
        assertThat(actualGroupIdInSearchGroup, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun deleteSearchGroupCallsRemoveOfUserWithCorrectGroupId() {
        val calledUserRepository = CountDownLatch(1)

        lateinit var actualGroupIdInUser: String

        val userRepo = object : EmptyMockUserRepo() {
            override fun removeAllUserOfSearchGroup(searchGroupId: String) {
                actualGroupIdInUser = searchGroupId
                calledUserRepository.countDown()
            }
        }

        UserRepositoryProvider.provide = { userRepo }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledUserRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledUserRepository.count, equalTo(0L))
        assertThat(actualGroupIdInUser, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun deleteSearchGroupCallsRemoveOfMarkerWithCorrectGroupId() {
        val calledMarkerRepository = CountDownLatch(1)

        lateinit var actualGroupIdInMarker: String

        val markerRepo = object : EmptyMockMarkerRepo() {
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
                actualGroupIdInMarker = searchGroupId
                calledMarkerRepository.countDown()
            }
        }

        MarkerRepositoryProvider.provide = { markerRepo }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledMarkerRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledMarkerRepository.count, equalTo(0L))
        assertThat(actualGroupIdInMarker, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun deleteSearchGroupCallsRemoveOfHeatmapWithCorrectGroupId() {
        val calledHeatmapRepository = CountDownLatch(1)

        lateinit var actualGroupIdInHeatmap: String

        val heatmapRepo = object : EmptyMockHeatmapRepo() {
            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
                actualGroupIdInHeatmap = searchGroupId
                calledHeatmapRepository.countDown()
            }
        }

        HeatmapRepositoryProvider.provide = { heatmapRepo }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledHeatmapRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledHeatmapRepository.count, equalTo(0L))
        assertThat(actualGroupIdInHeatmap, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun getAllGroupsCallsGetAllGroups() {
        val called = CountDownLatch(1)

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun getAllGroups(): MutableLiveData<List<SearchGroupData>> {
                called.countDown()
                return MutableLiveData()
            }
        }

        SearchGroupRepositoryProvider.provide = { searchGroupRepo }

        SearchGroupDataManager().getAllGroups()
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun getGroupByIdCallsGetGroupByIdWithCorrectSearchGroupData() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID

        lateinit var actualGroupId: String

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
                called.countDown()
                actualGroupId = groupId
                return MutableLiveData()
            }
        }

        SearchGroupRepositoryProvider.provide = { searchGroupRepo }

        SearchGroupDataManager().getGroupById(expectedGroupId)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
        assertThat(actualGroupId, equalTo(expectedGroupId))
    }

    @Test
    fun editGroupCallsUpdateGroup() {
        val called = CountDownLatch(1)

        val expectedGroupData = SearchGroupData(DUMMY_GROUP_ID)

        lateinit var actualGroupData: SearchGroupData

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun updateGroup(searchGroupData: SearchGroupData) {
                called.countDown()
                actualGroupData = searchGroupData
            }
        }

        SearchGroupRepositoryProvider.provide = { searchGroupRepo }

        SearchGroupDataManager().editGroup(expectedGroupData)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
        assertThat(actualGroupData, equalTo(expectedGroupData))
    }

    @Test
    fun getOperatorsOfSearchGroupCallsGetOperatorsOfSearchGroupWithCorrectSearchGroupId() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID
        lateinit var actualGroupId: String

        val userRepo = object : EmptyMockUserRepo() {
            override fun getOperatorsOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
                called.countDown()
                actualGroupId = searchGroupId
                return MutableLiveData()
            }
        }

        UserRepositoryProvider.provide = { userRepo }

        SearchGroupDataManager().getOperatorsOfSearchGroup(expectedGroupId)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
        assertThat(actualGroupId, equalTo(expectedGroupId))
    }

    @Test
    fun getRescuersOfSearchGroupCallsGetRescuersOfSearchGroupWithCorrectSearchGroupId() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID
        lateinit var actualGroupId: String

        val userRepo = object : EmptyMockUserRepo() {
            override fun getRescuersOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
                called.countDown()
                actualGroupId = searchGroupId
                return MutableLiveData()
            }
        }

        UserRepositoryProvider.provide = { userRepo }

        SearchGroupDataManager().getRescuersOfSearchGroup(expectedGroupId)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
        assertThat(actualGroupId, equalTo(expectedGroupId))
    }

    @Test
    fun removeUserOfSearchGroupCallsRemoveUserFromSearchGroupWithCorrectGroupIdAndUserId() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID
        val expectedUserId = DUMMY_USER_ID

        lateinit var actualGroupId: String
        lateinit var actualUserId: String

        val userRepo = object : EmptyMockUserRepo() {
            override fun removeUserFromSearchGroup(searchGroupId: String, userId: String) {
                called.countDown()
                actualGroupId = searchGroupId
                actualUserId = userId
            }
        }

        UserRepositoryProvider.provide = { userRepo }

        SearchGroupDataManager().removeUserOfSearchGroup(expectedGroupId, expectedUserId)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
        assertThat(actualGroupId, equalTo(expectedGroupId))
    }

    @Test
    fun addUserToSearchgroupCallsAddUserToSearchGroupWithCorrectSearchGroupIdAndUser() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID
        val expectedUser = UserData(email = DUMMY_EMAIL, role = DUMMY_ROLE)

        lateinit var actualGroupId: String
        lateinit var actualUser: UserData

        val userRepo = object : EmptyMockUserRepo() {
            override fun addUserToSearchGroup(searchGroupId: String, user: UserData) {
                called.countDown()
                actualGroupId = searchGroupId
                actualUser = user
            }
        }

        UserRepositoryProvider.provide = { userRepo }

        SearchGroupDataManager().addUserToSearchgroup(expectedGroupId, DUMMY_EMAIL, DUMMY_ROLE)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
        assertThat(actualGroupId, equalTo(expectedGroupId))
        assertThat(actualUser, equalTo(expectedUser))
    }

//    @Test
//    fun createSearchGroupCallsCreateSearchGroup() {
//        TODO("Not implemented yet")
//    }
//
//    @Test
//    fun createSearchGroupCallsCreateUser() {
//        TODO("Not implemented yet")
//    }
}
