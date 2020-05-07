package ch.epfl.sdp.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import ch.epfl.sdp.database.data.SearchGroupData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseGroupDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private const val ASYNC_CALL_TIMEOUT = 5L
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
    }

    @Before
    fun beforeAll() {
        Firebase.database.goOffline()
        Firebase.database.reference.removeValue()
    }

    @Test
    fun getSearchGroupsReturnsExpectedValues() {
        val dao = FirebaseGroupDao()
        val expectedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        val tested = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("search_groups/$DUMMY_GROUP_ID")
                .setValue(expectedGroup)

        //Validate g1 data
        val data = dao.getGroups()
        data.observeForever { groups ->
            // Test once database has been populated
            if (groups != null && groups.find { it.uuid == DUMMY_GROUP_ID } != null) {
                val actualGroup = groups.find { it.uuid == DUMMY_GROUP_ID }

                MatcherAssert.assertThat(actualGroup, equalTo(expectedGroup))
                tested.countDown()
            }
        }

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        MatcherAssert.assertThat(tested.count, equalTo(0L))
    }

    @Test
    fun getSearchGroupsByIdReturnsExpectedValues() {
        val dao = FirebaseGroupDao()
        val expectedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        val tested = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("search_groups/$DUMMY_GROUP_ID")
                .setValue(expectedGroup)

        //Validate g1 data
        val data = dao.getGroupById(DUMMY_GROUP_ID)
        data.observeForever { group ->
            // Test once database has been populated
            if (group != null) {
                MatcherAssert.assertThat(group, equalTo(expectedGroup))
                tested.countDown()
            }
        }

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        MatcherAssert.assertThat(tested.count, equalTo(0L))
    }

//    @Test
//    fun updateHeatmapUpdatesHeatmap() {
//        val dao = FirebaseHeatmapDao()
//        val initialHeatmap = HeatmapData(mutableListOf(
//                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
//                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
//        ), DUMMY_HEATMAP_ID)
//        val expectedHeatmap = HeatmapData(mutableListOf(
//                HeatmapPointData(LatLng(43.0, 10.0), 10.0)
//        ), DUMMY_HEATMAP_ID)
//        val tested = CountDownLatch(1)
//        var initialData = true
//
//        //Populate database
//        Firebase.database.getReference("heatmaps/$DUMMY_GROUP_ID/$DUMMY_HEATMAP_ID").setValue(initialHeatmap)
//
//        //Testing
//        val ref = Firebase.database.getReference("heatmaps/$DUMMY_GROUP_ID/$DUMMY_HEATMAP_ID")
//        val listener = object : ValueEventListener {
//            override fun onCancelled(dataSnapshot: DatabaseError) {}
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (!initialData) {
//                    val actualHeatmap = dataSnapshot.getValue(HeatmapData::class.java)!!
//                    // We do not want to compare uuids as they are generated at adding time by firebase
//                    actualHeatmap.uuid = dataSnapshot.key
//
//                    MatcherAssert.assertThat(actualHeatmap, equalTo(expectedHeatmap))
//                    tested.countDown()
//                } else {
//                    initialData = false
//                }
//            }
//        }
//        ref.addValueEventListener(listener)
//
//        //Update value
//        dao.updateHeatmap(DUMMY_GROUP_ID, expectedHeatmap)
//
//        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
//        MatcherAssert.assertThat(tested.count, equalTo(0L))
//        ref.removeEventListener(listener)
//    }
}