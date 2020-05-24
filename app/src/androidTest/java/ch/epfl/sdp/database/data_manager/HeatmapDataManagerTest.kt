package ch.epfl.sdp.database.data_manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.repository.IHeatmapRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class HeatmapDataManagerTest {

    companion object {
        private const val DUMMY_HEATMAP_ID = "Dummy_heatmap_id"
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_INTENSITY = 8.75
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
    }


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun addMeasureToHeatmapForNewHeatmapCallsUpdateHeatmap() {
        val expectedHeatMapData = HeatmapData(mutableListOf(
                HeatmapPointData(DUMMY_LOCATION, DUMMY_INTENSITY)
        ), DUMMY_HEATMAP_ID)
        val expectedGroupId = DUMMY_GROUP_ID

        val repo = Mockito.mock(IHeatmapRepository::class.java)
        Mockito.`when`(repo.getGroupHeatmaps(expectedGroupId)).thenReturn(MutableLiveData(mutableMapOf()))

        HeatmapRepositoryProvider.provide = { repo }
        HeatmapDataManager().addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION, DUMMY_INTENSITY)

        Mockito.verify(repo, Mockito.times(1)).getGroupHeatmaps(expectedGroupId)
        Mockito.verify(repo, Mockito.times(1)).updateHeatmap(expectedGroupId, expectedHeatMapData)
    }

    @Test
    fun addMeasureToHeatmapForExistingHeatmapCallsUpdateHeatmap() {
        val expectedHeatMapData = HeatmapData(mutableListOf(
                HeatmapPointData(DUMMY_LOCATION, DUMMY_INTENSITY)
        ), DUMMY_HEATMAP_ID)
        val expectedGroupId = DUMMY_GROUP_ID
        val previousHeatMapData = HeatmapData(mutableListOf(), DUMMY_HEATMAP_ID)

        val repo = Mockito.mock(IHeatmapRepository::class.java)
        Mockito.`when`(repo.getGroupHeatmaps(expectedGroupId)).thenReturn(MutableLiveData(mutableMapOf(Pair(DUMMY_HEATMAP_ID, MutableLiveData(previousHeatMapData)))))

        HeatmapRepositoryProvider.provide = { repo }
        HeatmapDataManager().addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION, DUMMY_INTENSITY)

        Mockito.verify(repo, Mockito.times(1)).getGroupHeatmaps(expectedGroupId)
        Mockito.verify(repo, Mockito.times(1)).updateHeatmap(expectedGroupId, expectedHeatMapData)
    }

    @Test
    fun removeAllHeatmapsOfSearchCallsGroupRemoveAllHeatmapsOfSearch() {
        val expectedGroupId = DUMMY_GROUP_ID

        val repo = Mockito.mock(IHeatmapRepository::class.java)

        HeatmapRepositoryProvider.provide = { repo }
        HeatmapDataManager().removeAllHeatmapsOfSearchGroup(DUMMY_GROUP_ID)

        Mockito.verify(repo, Mockito.times(1)).removeAllHeatmapsOfSearchGroup(expectedGroupId)
    }

    @Test
    fun getGroupHeatmpasCallsgetGroupsHeatmaps() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedHeatMapData =
                MutableLiveData(mutableMapOf(Pair(DUMMY_HEATMAP_ID, MutableLiveData(HeatmapData(mutableListOf(
                        HeatmapPointData(DUMMY_LOCATION, DUMMY_INTENSITY)
                ), DUMMY_HEATMAP_ID)))))

        val repo = Mockito.mock(IHeatmapRepository::class.java)
        Mockito.`when`(repo.getGroupHeatmaps(expectedGroupId)).thenReturn(expectedHeatMapData)

        HeatmapRepositoryProvider.provide = { repo }

        assertThat(HeatmapDataManager().getGroupHeatmaps(expectedGroupId), equalTo(expectedHeatMapData as LiveData<MutableMap<String, MutableLiveData<HeatmapData>>>))
        Mockito.verify(repo, Mockito.times(1)).getGroupHeatmaps(expectedGroupId)
    }
}