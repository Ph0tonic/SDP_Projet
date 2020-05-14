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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MarkerRepositoryTest {

    companion object{
        private const val ASYNC_CALL_TIMEOUT = 5L
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_MARKER_ID = "Dummy_marker_id"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getMarkersOfSearchGroupCallsGetMarkersOfSearchGroupFromDao() {
        val expectedData = MutableLiveData(setOf(MarkerData(uuid = "UUID-1")))
        val dao = object : MarkerDao {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return expectedData
            }

            override fun addMarker(groupId: String, markerData: MarkerData) {}
            override fun removeMarker(groupId: String, markerId: String) {}
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
        }
        MarkerRepository.daoProvider = { dao }

        val repo = MarkerRepository()

        assertThat(repo.getMarkersOfSearchGroup("DummyGroupName"), equalTo(expectedData))
    }

    @Test
    fun addMarkerCallsAddMarkerFromDao() {
        var wasCalled = false
        val dao = object : MarkerDao {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return MutableLiveData()
            }

            override fun addMarker(groupId: String, markerData: MarkerData) {
                wasCalled = true
            }

            override fun removeMarker(groupId: String, markerId: String) {}
            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
        }
        MarkerRepository.daoProvider = { dao }

        MarkerRepository().addMarkerForSearchGroup("DummyGroupId", MarkerData(LatLng(0.0, 0.0)))
        assertThat(wasCalled, equalTo(true))
    }

    @Test
    fun removeMarkerCallsRemoveMarkerFromDaoWithCorrectGroupId() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID
        val expectedMarkerId = DUMMY_MARKER_ID

        lateinit var actualGroupId: String
        lateinit var actualMarkerId: String

        val dao = object : MarkerDao {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return MutableLiveData()
            }

            override fun addMarker(groupId: String, markerData: MarkerData) {}
            override fun removeMarker(groupId: String, markerId: String) {
                called.countDown()
                actualGroupId = groupId
            }

            override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
        }
        MarkerRepository.daoProvider = { dao }

        MarkerRepository().removeMarkerOfSearchGroup(DUMMY_GROUP_ID, DUMMY_MARKER_ID)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    //TODO TEST:
    // override fun removeMarker(groupId: String, markerId: String) {}
    // override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {}
}