package ch.epfl.sdp.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineHeatmapDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_name"
        private const val DUMMY_HEATMAP_ID = "Dummy_heatmap_id"
    }

    @Before
    fun beforeAll() {
        Firebase.database.goOffline()
        Firebase.database.reference.removeValue()
    }

    @Test
    fun getHeatmapOfSearchGroupReturnsExpectedValues() {
        val dao = OfflineHeatmapDao()
        val heatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
        ), DUMMY_HEATMAP_ID)

        dao.updateHeatmap(DUMMY_GROUP_ID, heatmap)
        val data = dao.getHeatmapsOfSearchGroup(DUMMY_GROUP_ID)

        MatcherAssert.assertThat(data.value!!.size, equalTo(1))
    }

    @Test
    fun updateHeatmapUpdatesHeatmap() {
        val dao = OfflineHeatmapDao()
        val initialHeatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
        ), DUMMY_HEATMAP_ID)
        val expectedHeatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(43.0, 10.0), 10.0)
        ), DUMMY_HEATMAP_ID)

        //Populate database
        Firebase.database.getReference("heatmaps/$DUMMY_GROUP_ID/$DUMMY_HEATMAP_ID").setValue(initialHeatmap)

        //Update value
        dao.updateHeatmap(DUMMY_GROUP_ID, expectedHeatmap)
        val data = dao.getHeatmapsOfSearchGroup(DUMMY_GROUP_ID)

        assertThat(data.value!![DUMMY_HEATMAP_ID]!!.value, equalTo(expectedHeatmap))
    }

    @Test
    fun removeAllHeatmapsOfSearchGroupRemoveAllHeatmapsOfSearchGroup() {
        val dao = OfflineHeatmapDao()
        val initialHeatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
        ), DUMMY_HEATMAP_ID)
        val expectedHeatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(43.0, 10.0), 10.0)
        ), DUMMY_HEATMAP_ID)

        //Update value
        dao.updateHeatmap(DUMMY_GROUP_ID, initialHeatmap)
        dao.updateHeatmap(DUMMY_GROUP_ID, expectedHeatmap)
        val data = dao.getHeatmapsOfSearchGroup(DUMMY_GROUP_ID)

        assertThat(data.value!![DUMMY_HEATMAP_ID]!!.value, equalTo(expectedHeatmap))

        dao.removeAllHeatmapsOfSearchGroup(DUMMY_GROUP_ID)
        assertThat(data.value!!.size, equalTo(0))
    }
}