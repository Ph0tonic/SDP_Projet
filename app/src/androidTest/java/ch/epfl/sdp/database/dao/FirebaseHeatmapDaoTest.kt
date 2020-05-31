package ch.epfl.sdp.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
class FirebaseHeatmapDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_name"
        private const val DUMMY_HEATMAP_ID = "Dummy_heatmap_id"
        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @Before
    fun beforeAll() {
        Firebase.database.goOffline()
        Firebase.database.reference.removeValue()
    }

    @Test
    fun getHeatmapOfSearchGroupReturnsExpectedValues() {
        val dao = FirebaseHeatmapDao()
        val heatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
        ), DUMMY_HEATMAP_ID)
        val called = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("heatmaps/$DUMMY_GROUP_ID/$DUMMY_HEATMAP_ID")
                .setValue(heatmap)

        //Validate g1 data
        val data = dao.getHeatmapsOfSearchGroup(DUMMY_GROUP_ID)
        data.observeForever {
            // Test once database has been populated
            if (it.containsKey(DUMMY_HEATMAP_ID) && it[DUMMY_HEATMAP_ID]!!.value != null) {
                val actualHeatmap = it[DUMMY_HEATMAP_ID]!!.value!!

                assertThat(actualHeatmap, equalTo(heatmap))
                called.countDown()
            }
        }

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun removeAllHeatmapsOfSearchGroupRemovesAllHeatmapsOfSearchGroup() {
        val dao = FirebaseHeatmapDao()
        val initialHeatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
        ), DUMMY_HEATMAP_ID)

        //Populate database
        Firebase.database.getReference("heatmaps/$DUMMY_GROUP_ID/$DUMMY_HEATMAP_ID").setValue(initialHeatmap)

        //Update value
        dao.updateHeatmap(DUMMY_GROUP_ID, initialHeatmap)
        dao.removeAllHeatmapsOfSearchGroup(DUMMY_GROUP_ID)
        val data = dao.getHeatmapsOfSearchGroup(DUMMY_GROUP_ID)

        assertThat(data.value!!.size, equalTo(0))
    }

    @Test
    fun updateHeatmapUpdatesHeatmap() {
        val dao = FirebaseHeatmapDao()
        val initialHeatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
        ), DUMMY_HEATMAP_ID)
        val expectedHeatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(43.0, 10.0), 10.0)
        ), DUMMY_HEATMAP_ID)
        val called = CountDownLatch(1)
        var initialData = true

        //Populate database
        Firebase.database.getReference("heatmaps/$DUMMY_GROUP_ID/$DUMMY_HEATMAP_ID").setValue(initialHeatmap)

        //Testing
        val ref = Firebase.database.getReference("heatmaps/$DUMMY_GROUP_ID/$DUMMY_HEATMAP_ID")
        val listener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!initialData) {
                    val actualHeatmap = dataSnapshot.getValue(HeatmapData::class.java)!!
                    // We do not want to compare uuids as they are generated at adding time by firebase
                    actualHeatmap.uuid = dataSnapshot.key

                    assertThat(actualHeatmap, equalTo(expectedHeatmap))
                    called.countDown()
                } else {
                    initialData = false
                }
            }
        }
        ref.addValueEventListener(listener)

        //Update value
        dao.updateHeatmap(DUMMY_GROUP_ID, expectedHeatmap)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
        ref.removeEventListener(listener)
    }
}