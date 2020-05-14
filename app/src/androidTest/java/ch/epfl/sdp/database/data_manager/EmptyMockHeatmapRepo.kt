package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.repository.IHeatmapRepository

open class EmptyMockHeatmapRepo : IHeatmapRepository {
    override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {}
    override fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        return MutableLiveData()
    }

    override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {}
}