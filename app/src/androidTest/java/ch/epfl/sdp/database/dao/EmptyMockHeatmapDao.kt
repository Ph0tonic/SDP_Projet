package ch.epfl.sdp.database.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData

open class EmptyMockHeatmapDao : HeatmapDao {
    override fun getHeatmapsOfSearchGroup(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        return MutableLiveData()
    }

    override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {}
    override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {}
}