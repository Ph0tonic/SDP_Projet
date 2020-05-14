package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.SearchGroupRepository
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SearchGroupDataManagerTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    //    fun deleteSearchGroup(searchGroupId: String)
    @Test
    fun deleteSearchGroupCallsRemoveOfSearchGroup() {
        val calledSearchGroupRepository = CountDownLatch(1)

        lateinit var actualGroupIdInSearchGroup: String

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun removeSearchGroup(searchGroupId: String) {
                actualGroupIdInSearchGroup = searchGroupId
                calledSearchGroupRepository.countDown()
            }
        }

        SearchGroupRepositoryProvider.provide = { searchGroupRepo }
        UserRepositoryProvider.provide = { EmptyMockUserRepo() }
        MarkerRepositoryProvider.provide = { EmptyMockMarkerRepo() }
        HeatmapRepositoryProvider.provide = { EmptyMockHeatmapRepo() }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledSearchGroupRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledSearchGroupRepository.count, equalTo(0L))
        assertThat(actualGroupIdInSearchGroup, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun deleteSearchGroupCallsRemoveOfUser() {
        val calledUserRepository = CountDownLatch(1)

        lateinit var actualGroupIdInUser: String

        val userRepo = object : EmptyMockUserRepo() {
            override fun removeAllUserOfSearchGroup(searchGroupId: String) {
                actualGroupIdInUser = searchGroupId
                calledUserRepository.countDown()
            }
        }

        SearchGroupRepositoryProvider.provide = { EmptyMockSearchGroupRepo() }
        UserRepositoryProvider.provide = { userRepo }
        MarkerRepositoryProvider.provide = { EmptyMockMarkerRepo() }
        HeatmapRepositoryProvider.provide = { EmptyMockHeatmapRepo() }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledUserRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledUserRepository.count, equalTo(0L))
        assertThat(actualGroupIdInUser, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun deleteSearchGroupCallsRemoveOfMarker() {
        val calledMarkerRepository = CountDownLatch(1)

        lateinit var actualGroupIdInMarker: String

        val markerRepo = object : EmptyMockMarkerRepo() {
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
                actualGroupIdInMarker = searchGroupId
                calledMarkerRepository.countDown()
            }
        }

        SearchGroupRepositoryProvider.provide = { EmptyMockSearchGroupRepo() }
        UserRepositoryProvider.provide = { EmptyMockUserRepo() }
        MarkerRepositoryProvider.provide = { markerRepo }
        HeatmapRepositoryProvider.provide = { EmptyMockHeatmapRepo() }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledMarkerRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledMarkerRepository.count, equalTo(0L))
        assertThat(actualGroupIdInMarker, equalTo(DUMMY_GROUP_ID))
    }

    @Test
    fun deleteSearchGroupCallsRemoveOfHeatmap() {
        val calledHeatmapRepository = CountDownLatch(1)

        lateinit var actualGroupIdInHeatmap: String

        val heatmapRepo = object : EmptyMockHeatmapRepo() {
            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
                actualGroupIdInHeatmap = searchGroupId
                calledHeatmapRepository.countDown()
            }
        }

        SearchGroupRepositoryProvider.provide = { EmptyMockSearchGroupRepo() }
        UserRepositoryProvider.provide = { EmptyMockUserRepo() }
        MarkerRepositoryProvider.provide = { EmptyMockMarkerRepo() }
        HeatmapRepositoryProvider.provide = { heatmapRepo }

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        calledHeatmapRepository.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(calledHeatmapRepository.count, equalTo(0L))
        assertThat(actualGroupIdInHeatmap, equalTo(DUMMY_GROUP_ID))
    }

    //    fun createSearchGroup(name: String): String
    @Test
    fun createSearchGroupCallsCreateSearchGroup() {
        TODO("Not implemented yet")
    }

    @Test
    fun createSearchGroupCallsCreateUser() {
        TODO("Not implemented yet")
    }

    @Test
    fun getAllGroupsCallsGetAllGroupsAndReturnsCorrectGroups() {
        val called = CountDownLatch(1)

        val expectedGroups = MutableLiveData(listOf(SearchGroupData(DUMMY_GROUP_ID)))

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun getAllGroups(): MutableLiveData<List<SearchGroupData>> {
                called.countDown()
                return expectedGroups
            }
        }

        SearchGroupRepositoryProvider.provide = {searchGroupRepo}

        val actualGroups = SearchGroupDataManager().getAllGroups()
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(called.count, equalTo(0L))
        assertThat(actualGroups.value, equalTo(expectedGroups.value))
    }
//    fun getGroupById(groupId: String): MutableLiveData<SearchGroupData>
//    fun editGroup(searchGroupData: SearchGroupData)
//    fun getOperatorsOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>>
//    fun getRescuersOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>>
//    fun removeUserOfSearchGroup(searchGroupId: String, userId: String)
//    fun addUserToSearchgroup(searchGroupId: String, email: String, role: Role)


}