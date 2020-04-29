package ch.epfl.sdp.database.repository

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.MarkersDao
import ch.epfl.sdp.database.data.MarkerData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkerRepositoryTest {

    @Test
    fun getMarkersOfSearchGroupCallsGetMarkersOfSearchGroupFromDao() {
        val expectedData = MutableLiveData(setOf(MarkerData(uuid = "UUID-1")))
        val dao = object : MarkersDao {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return expectedData
            }

            override fun addMarker(groupId: String, markerData: MarkerData) {}
            override fun removeMarker(groupId: String, markerId: String) {}
        }
        MarkerRepository.daoProvider = {dao}

        val repo = MarkerRepository()

        assertThat(repo.getMarkersOfSearchGroup("DummyGroupName"), equalTo(expectedData))
    }

    @Test
    fun addMarkerCallsAddMarkerFromDao() {
        var wasCalled = false
        val dao = object : MarkersDao {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return MutableLiveData()
            }
            override fun addMarker(groupId: String, markerData: MarkerData) {
                wasCalled = true
            }
            override fun removeMarker(groupId: String, markerId: String) {}
        }
        MarkerRepository.daoProvider = {dao}

        MarkerRepository().addMarkerForSearchGroup("DummyGroupId", LatLng(0.0, 0.0))
        assertThat(wasCalled, equalTo(true))
    }

    @Test
    fun removeMarkerCallsRemoveMarkerFromDao() {
        var wasCalled = false
        val dao = object : MarkersDao {
            override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
                return MutableLiveData()
            }
            override fun addMarker(groupId: String, markerData: MarkerData) {}
            override fun removeMarker(groupId: String, markerId: String) {
                wasCalled = true
            }
        }
        MarkerRepository.daoProvider = {dao}

        MarkerRepository().removeMarkerForSearchGroup("DummyGroupId","DummyMarkerId")
        assertThat(wasCalled, equalTo(true))
    }
}