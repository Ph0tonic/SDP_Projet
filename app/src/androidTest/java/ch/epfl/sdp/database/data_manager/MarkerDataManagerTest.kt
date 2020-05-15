package ch.epfl.sdp.database.data_manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.repository.IMarkerRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MarkerDataManagerTest {

    companion object {
        private const val DUMMY_HEATMAP_ID = "Dummy_heatmap_id"
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_MARKER_ID = "Dummy_marker_id"
        private const val DUMMY_INTENSITY = 8.75
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
        private const val ASYNC_CALL_TIMEOUT = 5L
    }


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getMarkersOfSearchGroupCallsGetMarkersOfSearchGroupAndGetsCorrectMarkers() {
        val called = CountDownLatch(1)

        val expectedMarkers = MutableLiveData(setOf(MarkerData(DUMMY_LOCATION, DUMMY_MARKER_ID)))

        val repo = object : IMarkerRepository {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                called.countDown()
                return expectedMarkers
            }

            override fun addMarkerForSearchGroup(groupId: String, marker: MarkerData) {}
            override fun removeMarkerOfSearchGroup(groupId: String, markerId: String) {}
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
        }

        MarkerRepositoryProvider.provide = { repo }
        val manager = MarkerDataManager()
        val actualMarkers = manager.getMarkersOfSearchGroup(DUMMY_GROUP_ID).value

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
        assertThat(actualMarkers, equalTo(expectedMarkers.value))
    }

    @Test
    fun addMarkerForSearchGroupCallsAddMarkerForSearchGroup() {
        val called = CountDownLatch(1)

        val expectedMarker = MarkerData(DUMMY_LOCATION)

        lateinit var actualMarker: MarkerData

        val repo = object : IMarkerRepository {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return MutableLiveData(setOf())
            }

            override fun addMarkerForSearchGroup(groupId: String, marker: MarkerData) {
                called.countDown()
                actualMarker = marker
            }

            override fun removeMarkerOfSearchGroup(groupId: String, markerId: String) {}
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
        }

        MarkerRepositoryProvider.provide = { repo }
        val manager = MarkerDataManager()
        manager.addMarkerForSearchGroup(DUMMY_GROUP_ID, DUMMY_LOCATION)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
        assertThat(actualMarker, equalTo(expectedMarker))
    }

    @Test
    fun removeMarkerForSearchGroupCallsRemoveMarkerOfSearchGroup() {
        val called = CountDownLatch(1)

        val repo = object : IMarkerRepository {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return MutableLiveData(setOf())
            }

            override fun addMarkerForSearchGroup(groupId: String, marker: MarkerData) {}
            override fun removeMarkerOfSearchGroup(groupId: String, markerId: String) {
                called.countDown()
            }

            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
        }

        MarkerRepositoryProvider.provide = { repo }

        MarkerDataManager().removeMarkerForSearchGroup(DUMMY_GROUP_ID, DUMMY_MARKER_ID)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun removeAllMarkersOfSearchGroupCallsRemoveAllMarkersOfSearchGroup() {
        val called = CountDownLatch(1)

        val expectedMarker = MarkerData(DUMMY_LOCATION)

        val repo = object : IMarkerRepository {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return MutableLiveData(setOf())
            }

            override fun addMarkerForSearchGroup(groupId: String, marker: MarkerData) {}
            override fun removeMarkerOfSearchGroup(groupId: String, markerId: String) {}
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
                called.countDown()
            }
        }

        MarkerRepositoryProvider.provide = { repo }

        MarkerDataManager().removeAllMarkersOfSearchGroup(DUMMY_GROUP_ID)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }
}