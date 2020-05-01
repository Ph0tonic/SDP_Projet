package ch.epfl.sdp.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.MarkerData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseMarkerDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    @Throws(Exception::class)
    fun beforeAll() {
        Firebase.database.goOffline()
    }

    @Test
    fun getMarkersOfSearchGroupReturnsExpectedValues() {
        val dao = FirebaseMarkersDao()
        val marker = MarkerData(LatLng(41.0, 10.0))
        val tested = CountDownLatch(1)

        //Populate database
        val g1Ref = Firebase.database.getReference("markers/g4")
        g1Ref.push().setValue(marker)

        //Validate g1 data
        val data = dao.getMarkersOfSearchGroup("g4")
        data.observeForever {
            // Test once database has been populated
            if (it.isNotEmpty()) {
                // Uuid is generated automatically so we don't test
                marker.uuid = it.first().uuid
                assertThat(it.firstOrNull(), equalTo(marker))
                tested.countDown()
            }
        }

        tested.await(5L, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))
    }

//    override fun addMarker(groupId: String, markerData: MarkerData) {
//        database.getReference("markers/$groupId").push().setValue(markerData)
//    }

//    override fun removeMarker(groupId: String, markerId: String) {
//        database.getReference("markers/$groupId/$markerId").removeValue()
//    }

}