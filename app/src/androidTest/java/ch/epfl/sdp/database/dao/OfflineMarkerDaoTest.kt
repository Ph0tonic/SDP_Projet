package ch.epfl.sdp.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.MarkerData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class OfflineMarkerDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_name"
        private const val DUMMY_MARKER_ID = "Dummy_marker_id"
        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @Before
    fun setup() {
        Firebase.database.goOffline()
        Firebase.database.reference.removeValue()
    }

    @Test
    fun getMarkersOfSearchGroupReturnsExpectedValues() {
        val dao = OfflineMarkerDao()
        val marker1 = MarkerData(LatLng(41.0, 10.0))
        val marker2 = MarkerData(LatLng(12.0, 7.0))

        //Populate database
        dao.addMarker(DUMMY_GROUP_ID, marker1)
        dao.addMarker(DUMMY_GROUP_ID, marker2)

        //Validate g1 data
        val data = dao.getMarkersOfSearchGroup(DUMMY_GROUP_ID)

        assertThat(data.value!!.map { it.uuid = null; it }.containsAll(setOf(marker1, marker2)), equalTo(true))
    }

    @Test
    fun addMarkerAddsMarker() {
        val dao = OfflineMarkerDao()
        val expectedMarker = MarkerData(LatLng(41.0, 10.0))
        val called = CountDownLatch(1)

        dao.addMarker(DUMMY_GROUP_ID, expectedMarker)
        val data = dao.getMarkersOfSearchGroup(DUMMY_GROUP_ID)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(data.value!!.first().uuid, notNullValue())
        data.value!!.first().uuid = null
        assertThat(data.value!!.first(), equalTo(expectedMarker))
    }

    @Test
    fun removeMarkerRemovesMarker() {
        val dao = OfflineMarkerDao()
        val expectedRemovedMarker = MarkerData(LatLng(41.0, 10.0), DUMMY_MARKER_ID)

        dao.addMarker(DUMMY_GROUP_ID, expectedRemovedMarker)
        val data = dao.getMarkersOfSearchGroup(DUMMY_GROUP_ID)
        dao.removeMarker(DUMMY_GROUP_ID, data.value!!.first().uuid!!)

        assertThat(data.value!!.size, equalTo(0))
    }

    @Test
    fun removeAllMarkersRemovesAllMarkers() {
        val dao = OfflineMarkerDao()
        val expectedRemovedMarker = MarkerData(LatLng(41.0, 10.0), DUMMY_MARKER_ID)

        dao.addMarker(DUMMY_GROUP_ID, expectedRemovedMarker)
        dao.removeAllMarkersOfSearchGroup(DUMMY_GROUP_ID)

        val data = dao.getMarkersOfSearchGroup(DUMMY_GROUP_ID)
        assertThat(data.value!!.size, equalTo(0))
    }
}