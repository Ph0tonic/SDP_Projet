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
            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {}

            override fun getHeatmapsOfSearchGroup(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
                return expectedData
            }
        }
        HeatmapRepository.daoProvider = { dao }
        val repo = HeatmapRepository()

        assertThat(repo.getGroupHeatmaps(DUMMY_GROUP_ID), equalTo(expectedData))
    }

    //TODO Test
    // updateHeatmap

}