package ch.epfl.sdp.database.data_manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.IHeatmapRepository
import ch.epfl.sdp.database.repository.IMarkerRepository
import ch.epfl.sdp.database.repository.ISearchGroupRepository
import ch.epfl.sdp.database.repository.IUserRepository
import ch.epfl.sdp.utils.Auth
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SearchGroupDataManagerTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private const val DUMMY_USER_ID = "Dummy_user_id"
        private const val ASYNC_CALL_TIMEOUT_MS = 500L
        private const val DUMMY_EMAIL = "test@test.com"
        private val DUMMY_ROLE = Role.RESCUER
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockSearchGroupRepo: ISearchGroupRepository
    private lateinit var mockHeatmapRepo: IHeatmapRepository
    private lateinit var mockMarkerRepo: IMarkerRepository
    private lateinit var mockUserRepo: IUserRepository

    @Before
    fun setup() {
        mockSearchGroupRepo = Mockito.mock(ISearchGroupRepository::class.java)
        mockHeatmapRepo = Mockito.mock(IHeatmapRepository::class.java)
        mockMarkerRepo = Mockito.mock(IMarkerRepository::class.java)
        mockUserRepo = Mockito.mock(IUserRepository::class.java)

        SearchGroupRepositoryProvider.provide = { mockSearchGroupRepo }
        UserRepositoryProvider.provide = { mockUserRepo }
        MarkerRepositoryProvider.provide = { mockMarkerRepo }
        HeatmapRepositoryProvider.provide = { mockHeatmapRepo }
    }

    @Test
    fun deleteSearchGroupRemovesEverythingLinked() {
        val expectedGroupId = DUMMY_GROUP_ID

        SearchGroupDataManager().deleteSearchGroup(DUMMY_GROUP_ID)

        Mockito.verify(mockSearchGroupRepo, Mockito.times(1)).removeSearchGroup(expectedGroupId)
        Mockito.verify(mockUserRepo, Mockito.times(1)).removeAllUserOfSearchGroup(expectedGroupId)
        Mockito.verify(mockHeatmapRepo, Mockito.times(1)).removeAllHeatmapsOfSearchGroup(expectedGroupId)
        Mockito.verify(mockMarkerRepo, Mockito.times(1)).removeAllMarkersOfSearchGroup(expectedGroupId)
    }

    @Test
    fun getAllGroupsCallsGetGroupsIdsOfUserByEmailAndGetAllGroups() {
        val dataAvailable = CountDownLatch(1)

        runOnUiThread {
            Auth.email.value = DUMMY_EMAIL
            Auth.loggedIn.value = true
        }

        val expectedGroupId = DUMMY_GROUP_ID
        val expectedEmail = DUMMY_EMAIL
        val expectedGroupIds = mapOf(Pair(DUMMY_GROUP_ID, Role.OPERATOR))
        val expectedGroups = listOf(SearchGroupData(expectedGroupId, DUMMY_GROUP_NAME, null, null))
        val expectedResults = listOf(Pair(expectedGroups[0], Role.OPERATOR))

        Mockito.`when`(mockUserRepo.getGroupIdsOfUserByEmail(expectedEmail)).thenReturn(MutableLiveData(expectedGroupIds))
        Mockito.`when`(mockSearchGroupRepo.getAllGroups()).thenReturn(MutableLiveData(expectedGroups))

        val groups = SearchGroupDataManager().getAllGroups()

        Mockito.verify(mockUserRepo, Mockito.timeout(ASYNC_CALL_TIMEOUT_MS).times(1)).getGroupIdsOfUserByEmail(expectedEmail)

        groups.observeForever {
            if (!it.isNullOrEmpty()) {
                dataAvailable.countDown()
            }
        }

        dataAvailable.await(ASYNC_CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        assertThat(dataAvailable.count, equalTo(0L))

        Mockito.verify(mockSearchGroupRepo, Mockito.timeout(ASYNC_CALL_TIMEOUT_MS).times(1)).getAllGroups()

        assertThat(groups.value!!, equalTo(expectedResults))
    }

    @Test
    fun getGroupByIdCallsGetGroupByIdWithCorrectSearchGroupData() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedData = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedData)

        assertThat(SearchGroupDataManager().getGroupById(expectedGroupId), equalTo(expectedData as LiveData<SearchGroupData>))
        Mockito.verify(mockSearchGroupRepo, Mockito.times(1)).getGroupById(expectedGroupId)
    }

    @Test
    fun editGroupCallsUpdateGroup() {
        val expectedGroupData = SearchGroupData(DUMMY_GROUP_ID)

        SearchGroupDataManager().editGroup(expectedGroupData)

        Mockito.verify(mockSearchGroupRepo, Mockito.times(1)).updateGroup(expectedGroupData)
    }

    @Test
    fun getOperatorsOfSearchGroupCallsGetOperatorsOfSearchGroupWithCorrectSearchGroupId() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedData = MutableLiveData(setOf<UserData>())

        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(expectedData)

        assertThat(SearchGroupDataManager().getOperatorsOfSearchGroup(expectedGroupId), equalTo(expectedData as LiveData<Set<UserData>>))
        Mockito.verify(mockUserRepo, Mockito.times(1)).getOperatorsOfSearchGroup(expectedGroupId)
    }

    @Test
    fun getRescuersOfSearchGroupCallsGetRescuersOfSearchGroupWithCorrectSearchGroupId() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedData = MutableLiveData(setOf<UserData>())

        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(expectedData)

        assertThat(SearchGroupDataManager().getRescuersOfSearchGroup(expectedGroupId), equalTo(expectedData as LiveData<Set<UserData>>))
        Mockito.verify(mockUserRepo, Mockito.times(1)).getRescuersOfSearchGroup(expectedGroupId)
    }

    @Test
    fun removeUserOfSearchGroupCallsRemoveUserFromSearchGroupWithCorrectGroupIdAndUserId() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedUserId = DUMMY_USER_ID

        SearchGroupDataManager().removeUserOfSearchGroup(expectedGroupId, expectedUserId)

        Mockito.verify(mockUserRepo, Mockito.times(1)).removeUserFromSearchGroup(expectedGroupId, expectedUserId)
    }

    @Test
    fun addUserToSearchgroupCallsAddUserToSearchGroupWithCorrectSearchGroupIdAndUser() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedUser = UserData(email = DUMMY_EMAIL, role = DUMMY_ROLE)

        SearchGroupDataManager().addUserToSearchGroup(expectedGroupId, DUMMY_EMAIL, DUMMY_ROLE)

        Mockito.verify(mockUserRepo, Mockito.times(1)).addUserToSearchGroup(expectedGroupId, expectedUser)
    }

    @Test
    fun createSearchGroupCallsCreateSearchGroupAndAddUser() {
        val expectedName = DUMMY_GROUP_NAME
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedGroup = SearchGroupData(name = expectedName)
        val expectedUser = UserData(email = DUMMY_EMAIL, role = Role.OPERATOR)

        runOnUiThread {
            Auth.email.value = expectedUser.email
            Auth.loggedIn.value = true
        }

        Mockito.`when`(mockSearchGroupRepo.createGroup(expectedGroup)).thenReturn(expectedGroupId)
        assertThat(SearchGroupDataManager().createSearchGroup(expectedName), equalTo(expectedGroupId))

        Mockito.verify(mockSearchGroupRepo, Mockito.times(1)).createGroup(expectedGroup)
        Mockito.verify(mockUserRepo, Mockito.times(1)).addUserToSearchGroup(expectedGroupId, expectedUser)
    }
}
