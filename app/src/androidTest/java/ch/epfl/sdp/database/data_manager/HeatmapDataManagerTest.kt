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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HeatmapDataManagerTest {

    companion object {
        private const val DUMMY_HEATMAP_ID = "Dummy_heatmap_id"
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_INTENSITY = 8.75
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
        private const val ASYNC_CALL_TIMEOUT = 5L
    }


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun addMeasureToHeatmapForNewHeatmapCallsUpdateHeatmap() {
        val tested = CountDownLatch(1)
        val expectedHeatMapData = HeatmapData(mutableListOf(
                HeatmapPointData(DUMMY_LOCATION, DUMMY_INTENSITY)
        ), DUMMY_HEATMAP_ID)
        lateinit var actualHeatmapData: HeatmapData

        val repo = object : IHeatmapRepository {
            override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
                actualHeatmapData = heatmapData
                tested.countDown()
            }

            override fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
                return MutableLiveData(mutableMapOf())
            }

            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {}
        }

        HeatmapRepositoryProvider.provide = { repo }
        val manager = HeatmapDataManager()
        manager.addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION, DUMMY_INTENSITY)

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualHeatmapData, equalTo(expectedHeatMapData))
    }

    @Test
    fun addMeasureToHeatmapForExistingHeatmapCallsUpdateHeatmap() {
        val tested = CountDownLatch(1)
        val expectedHeatMapData = HeatmapData(mutableListOf(
                HeatmapPointData(DUMMY_LOCATION, DUMMY_INTENSITY)
        ), DUMMY_HEATMAP_ID)
        val previousHeatMapData = HeatmapData(mutableListOf(), DUMMY_HEATMAP_ID)
        lateinit var actualHeatmapData: HeatmapData

        val repo = object : IHeatmapRepository {
            override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
                actualHeatmapData = heatmapData
                tested.countDown()
            }

            override fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
                return MutableLiveData(mutableMapOf(Pair(DUMMY_HEATMAP_ID, MutableLiveData(previousHeatMapData))))
            }

            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {}
        }

        HeatmapRepositoryProvider.provide = { repo }
        val manager = HeatmapDataManager()
        manager.addMeasureToHeatmap(DUMMY_GROUP_ID, DUMMY_HEATMAP_ID, DUMMY_LOCATION, DUMMY_INTENSITY)

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualHeatmapData, equalTo(expectedHeatMapData))
    }

    @Test
    fun removeAllHeatmapsOfSearchCallsGroupRemoveAllHeatmapsOfSearch() {
        val tested = CountDownLatch(1)

        val repo = object : IHeatmapRepository {
            override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {}
            override fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
                TODO("should not be used")
            }

            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
                tested.countDown()
            }
        }

        HeatmapRepositoryProvider.provide = { repo }
        val manager = HeatmapDataManager()
        manager.removeAllHeatmapsOfSearchGroup(DUMMY_GROUP_ID)

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))
    }

    @Test
    fun getGroupHeatmpasCallsgetGroupsHeatmaps() {
        val tested = CountDownLatch(1)
        val expectedHeatMapData =
                MutableLiveData(mutableMapOf(Pair(DUMMY_HEATMAP_ID, MutableLiveData(HeatmapData(mutableListOf(
                        HeatmapPointData(DUMMY_LOCATION, DUMMY_INTENSITY)
                ), DUMMY_HEATMAP_ID)))))

        val repo = object : IHeatmapRepository {
            override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {}

            override fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
                tested.countDown()
                return expectedHeatMapData
            }

            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {}
        }

        HeatmapRepositoryProvider.provide = { repo }
        val manager = HeatmapDataManager()
        val actualHeatmapData = manager.getGroupHeatmaps(DUMMY_GROUP_ID)

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(tested.count, equalTo(0L))

        assertThat(actualHeatmapData.value, equalTo(expectedHeatMapData.value))
    }
}