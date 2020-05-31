package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.SearchGroupDao
import ch.epfl.sdp.database.data.SearchGroupData
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class SearchGroupRepositoryTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getGroupsCallsGetGroupsFromSearchGroupDao() {
        val expectedData = MutableLiveData(listOf(
                SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME)
        ))

        val dao = Mockito.mock(SearchGroupDao::class.java)
        Mockito.`when`(dao.getGroups()).thenReturn(expectedData)

        SearchGroupRepository.daoProvider = { dao }
        val repo = SearchGroupRepository()

        assertThat(repo.getAllGroups(), equalTo(expectedData as LiveData<List<SearchGroupData>>))
        Mockito.verify(dao, Mockito.times(1)).getGroups()
    }

    @Test
    fun getGroupByIdCallsGetGroupByIdFromSearchGroupDao() {
        val expectedData = MutableLiveData(
                SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME)
        )
        val expectedGroupId = DUMMY_GROUP_ID

        val dao = Mockito.mock(SearchGroupDao::class.java)
        Mockito.`when`(dao.getGroupById(expectedGroupId)).thenReturn(expectedData)

        SearchGroupRepository.daoProvider = { dao }

        assertThat(SearchGroupRepository().getGroupById(DUMMY_GROUP_ID), equalTo(expectedData as LiveData<SearchGroupData>))
        Mockito.verify(dao, Mockito.times(1)).getGroupById(expectedGroupId)
    }

    @Test
    fun createGroupCallsCreateGroupDao() {
        val expectedData = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME)
        val expectedGroupId = DUMMY_GROUP_ID

        val dao = Mockito.mock(SearchGroupDao::class.java)
        Mockito.`when`(dao.createGroup(expectedData)).thenReturn(expectedGroupId)

        SearchGroupRepository.daoProvider = { dao }
        assertThat(SearchGroupRepository().createGroup(expectedData), equalTo(expectedGroupId))

        Mockito.verify(dao, Mockito.times(1)).createGroup(expectedData)
    }

    @Test
    fun updateGroupCallsUpdateGroupDao() {
        val expectedData = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME)

        val dao = Mockito.mock(SearchGroupDao::class.java)

        SearchGroupRepository.daoProvider = { dao }
        SearchGroupRepository().updateGroup(expectedData)

        Mockito.verify(dao, Mockito.times(1)).updateGroup(expectedData)
    }

    @Test
    fun removeSearchGroupCallsRemoveSearchGroupDao() {
        val expectedData = DUMMY_GROUP_ID

        val dao = Mockito.mock(SearchGroupDao::class.java)

        SearchGroupRepository.daoProvider = { dao }
        SearchGroupRepository().removeSearchGroup(expectedData)

        Mockito.verify(dao, Mockito.times(1)).removeSearchGroup(expectedData)
    }
}