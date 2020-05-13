package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.MarkerDao
import ch.epfl.sdp.database.dao.SearchGroupDao
import ch.epfl.sdp.database.data.MarkerData
import ch.epfl.sdp.database.data.SearchGroupData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchGroupRepositoryTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getGroupsCallsGetGroupsFromSearchGroupDao() {
        val expectedData = MutableLiveData(listOf(
                SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        ))
        val dao = object : SearchGroupDao {
            override fun getGroups(): MutableLiveData<List<SearchGroupData>> {
                return expectedData
            }

            override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
                return MutableLiveData()
            }
        }
        SearchGroupRepository.daoProvider = { dao }

        val repo = SearchGroupRepository()
        assertThat(repo.getGroups(), equalTo(expectedData))
    }

    @Test
    fun getGroupByIdCallsGetGroupByIdFromSearchGroupDao() {
        val expectedData = MutableLiveData(
                SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        )
        var wasCalled = false
        val dao = object : SearchGroupDao {
            override fun getGroups(): MutableLiveData<List<SearchGroupData>> {
                return MutableLiveData()
            }

            override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
                wasCalled = true
                return expectedData
            }
        }
        SearchGroupRepository.daoProvider = { dao }

        assertThat(SearchGroupRepository().getGroupById(DUMMY_GROUP_ID), equalTo(expectedData))
        assertThat(wasCalled, equalTo(true))
    }
}