package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.EmptyMockHeatmapDao
import ch.epfl.sdp.database.data.HeatmapData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class HeatmapRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private val DUMMY_LOCATION_1 = LatLng(13.0, 42.0)
        val DUMMY_LOCATION_2 = LatLng(12.0, 42.0)
        private const val DUMMY_INTENSITY_1 = 666.0
        private const val DUMMY_INTENSITY_2 = 333.0
        private const val DUMMY_HEATMAP_ID = "dummy heatmap id"
        private const val DUMMY_GROUP_ID = "dummy_group_id"
        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @Test
    fun getGroupHeatmapsCallsGetGroupHeatmapsFromDao() {
        val called = CountDownLatch(1)
        val expectedData: LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> = MutableLiveData(mutableMapOf())

        val dao = object : EmptyMockHeatmapDao() {
            override fun getHeatmapsOfSearchGroup(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
                called.countDown()
                return expectedData
            }
        }

        HeatmapRepository.daoProvider = { dao }

        val repo = HeatmapRepository()
        val actualData = repo.getGroupHeatmaps(DUMMY_GROUP_ID)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualData, equalTo(expectedData))
    }

    @Test
    fun updateHeatmapCallsUpdateHeatmapWitCorrectGroupIdAndHeatmapData() {
        val called = CountDownLatch(1)

        val expectedGroupId = DUMMY_GROUP_ID
        val expectedHeatmapData = HeatmapData(uuid = DUMMY_HEATMAP_ID)

        lateinit var actualGroupId: String
        lateinit var actualHeatmapData: HeatmapData

        val dao = object : EmptyMockHeatmapDao() {
            override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
                called.countDown()
                actualGroupId = groupId
                actualHeatmapData = heatmapData
            }
        }

        HeatmapRepository.daoProvider = { dao }

        HeatmapRepository().updateHeatmap(expectedGroupId, expectedHeatmapData)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(expectedGroupId))
        assertThat(actualHeatmapData, equalTo(expectedHeatmapData))
    }

    @Test
    fun removeAllHeatmapsOfSearchGroupCallsRemoveAllHeatmapsOfSearchGroupDao() {
        val called = CountDownLatch(1)
        val expectedGroupId = DUMMY_GROUP_ID

        lateinit var actualGroupId: String

        val dao = object : EmptyMockHeatmapDao() {
            override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
                called.countDown()
                actualGroupId = searchGroupId
            }
        }

        HeatmapRepository.daoProvider = { dao }

        HeatmapRepository().removeAllHeatmapsOfSearchGroup(expectedGroupId)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualGroupId, equalTo(expectedGroupId))
    }
}