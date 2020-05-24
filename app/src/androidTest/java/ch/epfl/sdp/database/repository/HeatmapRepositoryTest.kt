package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.HeatmapDao
import ch.epfl.sdp.database.data.HeatmapData
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class HeatmapRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val DUMMY_HEATMAP_ID = "dummy heatmap id"
        private const val DUMMY_GROUP_ID = "dummy_group_id"
    }

    @Test
    fun getGroupHeatmapsCallsGetGroupHeatmapsFromDao() {
        val expectedData: LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> = MutableLiveData(mutableMapOf())

        val dao = Mockito.mock(HeatmapDao::class.java)
        Mockito.`when`(dao.getHeatmapsOfSearchGroup(DUMMY_GROUP_ID)).thenReturn(expectedData)

        HeatmapRepository.daoProvider = { dao }

        val repo = HeatmapRepository()
        assertThat(repo.getGroupHeatmaps(DUMMY_GROUP_ID), equalTo(expectedData))

        Mockito.verify(dao, Mockito.times(1)).getHeatmapsOfSearchGroup(DUMMY_GROUP_ID)
    }

    @Test
    fun updateHeatmapCallsUpdateHeatmapWitCorrectGroupIdAndHeatmapData() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedHeatmapData = HeatmapData(uuid = DUMMY_HEATMAP_ID)

        val dao = Mockito.mock(HeatmapDao::class.java)
        HeatmapRepository.daoProvider = { dao }

        HeatmapRepository().updateHeatmap(expectedGroupId, expectedHeatmapData)
        Mockito.verify(dao, Mockito.times(1)).updateHeatmap(expectedGroupId, expectedHeatmapData)
    }

    @Test
    fun removeAllHeatmapsOfSearchGroupCallsRemoveAllHeatmapsOfSearchGroupDao() {
        val expectedGroupId = DUMMY_GROUP_ID

        val dao = Mockito.mock(HeatmapDao::class.java)
        HeatmapRepository.daoProvider = { dao }

        HeatmapRepository().removeAllHeatmapsOfSearchGroup(expectedGroupId)
        Mockito.verify(dao, Mockito.times(1)).removeAllHeatmapsOfSearchGroup(expectedGroupId)
    }
}