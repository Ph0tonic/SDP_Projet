package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData

interface IHeatmapRepository {
    fun updateHeatmap(groupId: String, heatmapData: HeatmapData)
    fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>>
    fun removeAllHeatmapsOfSearchGroup(searchGroupId: String)
}

