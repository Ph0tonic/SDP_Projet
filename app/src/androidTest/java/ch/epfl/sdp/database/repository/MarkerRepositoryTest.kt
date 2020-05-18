package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.EmptyMockMarkerDao
import ch.epfl.sdp.database.data.MarkerData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MarkerRepositoryTest {

    companion object {
        private const val ASYNC_CALL_TIMEOUT = 5L
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_MARKER_ID = "Dummy_marker_id"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getMarkersOfSearchGroupCallsGetMarkersOfSearchGroupFromDao() {
        val called = CountDownLatch(1)
        val expectedData = MutableLiveData(setOf(MarkerData(uuid = DUMMY_MARKER_ID)))

        val dao = object : EmptyMockMarkerDao() {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                called.countDown()
                return expectedData
            }
        }
        MarkerRepository.daoProvider = { dao }
        val repo = MarkerRepository()

        assertThat(repo.getMarkersOfSearchGroup(DUMMY_GROUP_ID), equalTo(expectedData))
    }

    @Test
    fun addMarkerCallsAddMarkerFromDao() {
        val called = CountDownLatch(1)

        val dao = object : EmptyMockMarkerDao() {
            override fun addMarker(groupId: String, markerData: MarkerData) {
                called.countDown()
            }
        }

        MarkerRepository.daoProvider = { dao }
        MarkerRepository().addMarkerForSearchGroup(DUMMY_GROUP_ID, MarkerData(LatLng(0.0, 0.0)))

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun removeMarkerCallsRemoveMarkerFromDaoWithCorrectGroupId() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID
        val expectedMarkerId = DUMMY_MARKER_ID

        lateinit var actualGroupId: String
        lateinit var actualMarkerId: String

        val dao = object : EmptyMockMarkerDao() {
            override fun removeMarker(groupId: String, markerId: String) {
                actualGroupId = groupId
                actualMarkerId = markerId
                called.countDown()
            }
        }

        MarkerRepository.daoProvider = { dao }
        MarkerRepository().removeMarkerOfSearchGroup(expectedGroupId, expectedMarkerId)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(expectedGroupId))
        assertThat(actualMarkerId, equalTo(expectedMarkerId))
    }

    @Test
    fun removeAllMarkersCallsRemoveAllMarkersFromDaoWithCorrectGroupId() {
        val called = CountDownLatch(1)
        val expectedGroupId = DUMMY_GROUP_ID

        lateinit var actualGroupId: String

        val dao = object : EmptyMockMarkerDao() {
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
                actualGroupId = searchGroupId
                called.countDown()
            }
        }

        MarkerRepository.daoProvider = { dao }
        MarkerRepository().removeAllMarkersOfSearchGroup(expectedGroupId)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(expectedGroupId))
    }
}