package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.HeatmapDao
import ch.epfl.sdp.database.data.HeatmapData
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeatmapRepositoryTest {

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

        assertThat(repo.getGroupHeatmaps("DummyGroupName"), equalTo(expectedData))
    }
}