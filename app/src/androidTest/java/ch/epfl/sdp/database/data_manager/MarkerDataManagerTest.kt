package ch.epfl.sdp.database.data_manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.repository.IMarkerRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class MarkerDataManagerTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_MARKER_ID = "Dummy_marker_id"
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getMarkersOfSearchGroupCallsGetMarkersOfSearchGroupAndGetsCorrectMarkers() {
        val expectedMarkers = MutableLiveData(setOf(MarkerData(DUMMY_LOCATION, DUMMY_MARKER_ID)))
        val expectedGroupId = DUMMY_GROUP_ID

        val repo = Mockito.mock(IMarkerRepository::class.java)
        Mockito.`when`(repo.getMarkersOfSearchGroup(expectedGroupId)).thenReturn(expectedMarkers)

        MarkerRepositoryProvider.provide = { repo }
        val manager = MarkerDataManager()

        assertThat(manager.getMarkersOfSearchGroup(expectedGroupId), equalTo(expectedMarkers as LiveData<Set<MarkerData>>))
        Mockito.verify(repo, Mockito.times(1)).getMarkersOfSearchGroup(expectedGroupId)
    }

    @Test
    fun addMarkerForSearchGroupCallsAddMarkerForSearchGroup() {
        val expectedMarker = MarkerData(DUMMY_LOCATION)
        val expectedGroupId = DUMMY_GROUP_ID

        val repo = Mockito.mock(IMarkerRepository::class.java)

        MarkerRepositoryProvider.provide = { repo }
        MarkerDataManager().addMarkerForSearchGroup(expectedGroupId, DUMMY_LOCATION)

        Mockito.verify(repo, Mockito.times(1)).addMarkerForSearchGroup(expectedGroupId, expectedMarker)
    }

    @Test
    fun removeMarkerForSearchGroupCallsRemoveMarkerOfSearchGroup() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedMarkerId = DUMMY_MARKER_ID

        val repo = Mockito.mock(IMarkerRepository::class.java)
        MarkerRepositoryProvider.provide = { repo }

        MarkerDataManager().removeMarkerForSearchGroup(DUMMY_GROUP_ID, DUMMY_MARKER_ID)
        Mockito.verify(repo, Mockito.times(1)).removeMarkerOfSearchGroup(expectedGroupId, expectedMarkerId)
    }

    @Test
    fun removeAllMarkersOfSearchGroupCallsRemoveAllMarkersOfSearchGroup() {
        val expectedGroupId = DUMMY_GROUP_ID

        val repo = Mockito.mock(IMarkerRepository::class.java)
        MarkerRepositoryProvider.provide = { repo }

        MarkerDataManager().removeAllMarkersOfSearchGroup(expectedGroupId)
        Mockito.verify(repo, Mockito.times(1)).removeAllMarkersOfSearchGroup(expectedGroupId)
    }
}