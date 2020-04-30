package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.HeatmapDao
import ch.epfl.sdp.database.dao.MockHeatmapDao
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeatmapRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        val DUMMY_LOCATION_1 = LatLng(13.0, 42.0)
        val DUMMY_LOCATION_2 = LatLng(12.0, 42.0)
        const val DUMMY_INTENSITY_1 = 666.0
        const val DUMMY_INTENSITY_2 = 333.0
        const val DUMMY_HEATMAP_ID = "dummy heatmap id"
        const val DUMMY_GROUP_ID = "dummy_group_id"
    }

    @Test
    fun getGroupHeatmapsCallsGetGroupHeatmapsFromDao() {
        val expectedData: LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> = MutableLiveData(mutableMapOf())

        val dao = object : HeatmapDao {
            override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {}
            override fun getHeatmapsOfSearchGroup(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
                return expectedData
            }
        }
        HeatmapRepository.daoProvider = { dao }
        val repo = HeatmapRepository()

        assertThat(repo.getGroupHeatmaps(DUMMY_GROUP_ID), equalTo(expectedData))
    }

    @Test
    fun addMeasureToHeatmapForNewHeatmapCreateNewHeatmap() {
        val dao = MockHeatmapDao()
        HeatmapRepository.daoProvider = { dao }
        val repo = HeatmapRepository()
        repo.addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION_1, DUMMY_INTENSITY_1)

        assertThat(dao.data[DUMMY_GROUP_ID]!!.value!!.containsKey(DUMMY_HEATMAP_ID), equalTo(true))
    }

    @Test
    fun addMeasureToHeatmapForNewHeatmapCreateValidHeatmapData() {
        val dao = MockHeatmapDao()
        HeatmapRepository.daoProvider = { dao }
        val repo = HeatmapRepository()
        repo.addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION_1, DUMMY_INTENSITY_1)

        val expectedHeatmapData = HeatmapData(mutableListOf(HeatmapPointData(DUMMY_LOCATION_1, DUMMY_INTENSITY_1)), DUMMY_HEATMAP_ID)
        val heatmapData = dao.data[DUMMY_GROUP_ID]!!.value!![DUMMY_HEATMAP_ID]!!.value
        assertThat(heatmapData, equalTo(expectedHeatmapData))
    }

    @Test
    fun addMeasureToHeatmapForExistingHeatmapUpdateExistingHeatmap() {
        val dao = MockHeatmapDao()
        HeatmapRepository.daoProvider = { dao }
        val repo = HeatmapRepository()
        repo.addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION_1, DUMMY_INTENSITY_1)
        repo.addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION_2, DUMMY_INTENSITY_2)

        val expectedHeatmapData = HeatmapData(mutableListOf(
                HeatmapPointData(DUMMY_LOCATION_1, DUMMY_INTENSITY_1),
                HeatmapPointData(DUMMY_LOCATION_2, DUMMY_INTENSITY_2)
        ), DUMMY_HEATMAP_ID)
        val heatmapData = dao.data[DUMMY_GROUP_ID]!!.value!![DUMMY_HEATMAP_ID]!!.value
        assertThat(heatmapData, equalTo(expectedHeatmapData))
    }
}