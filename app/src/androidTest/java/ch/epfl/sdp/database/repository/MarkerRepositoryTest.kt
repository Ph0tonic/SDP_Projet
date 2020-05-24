package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.MarkerDao
import ch.epfl.sdp.database.data.MarkerData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class MarkerRepositoryTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_MARKER_ID = "Dummy_marker_id"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getMarkersOfSearchGroupCallsGetMarkersOfSearchGroupFromDao() {
        val expectedData = MutableLiveData(setOf(MarkerData(uuid = DUMMY_MARKER_ID)))
        val expectedGroupId = DUMMY_GROUP_ID

        val dao = Mockito.mock(MarkerDao::class.java)
        Mockito.`when`(dao.getMarkersOfSearchGroup(expectedGroupId)).thenReturn(expectedData)

        MarkerRepository.daoProvider = { dao }
        val repo = MarkerRepository()

        assertThat(repo.getMarkersOfSearchGroup(expectedGroupId), equalTo(expectedData))
        Mockito.verify(dao, Mockito.times(1)).getMarkersOfSearchGroup(expectedGroupId)
    }

    @Test
    fun addMarkerCallsAddMarkerFromDao() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedMarker = MarkerData(LatLng(0.0, 0.0))

        val dao = Mockito.mock(MarkerDao::class.java)
        MarkerRepository.daoProvider = { dao }

        MarkerRepository().addMarkerForSearchGroup(expectedGroupId, expectedMarker)
        Mockito.verify(dao, Mockito.times(1)).addMarker(expectedGroupId, expectedMarker)
    }

    @Test
    fun removeMarkerCallsRemoveMarkerFromDaoWithCorrectGroupId() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedMarkerId = DUMMY_MARKER_ID

        val dao = Mockito.mock(MarkerDao::class.java)
        MarkerRepository.daoProvider = { dao }

        MarkerRepository().removeMarkerOfSearchGroup(expectedGroupId, expectedMarkerId)
        Mockito.verify(dao, Mockito.times(1)).removeMarker(expectedGroupId, expectedMarkerId)
    }

    @Test
    fun removeAllMarkersCallsRemoveAllMarkersFromDaoWithCorrectGroupId() {
        val expectedGroupId = DUMMY_GROUP_ID

        val dao = Mockito.mock(MarkerDao::class.java)
        MarkerRepository.daoProvider = { dao }

        MarkerRepository().removeAllMarkersOfSearchGroup(expectedGroupId)

        Mockito.verify(dao, Mockito.times(1)).removeAllMarkersOfSearchGroup(expectedGroupId)
    }
}