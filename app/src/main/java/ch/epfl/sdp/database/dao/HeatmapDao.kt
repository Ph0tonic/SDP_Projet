package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData

interface HeatmapDao {

    fun updateHeatmap(groupId: String, heatmapData: HeatmapData)
    fun getHeatmapsOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, HeatmapData>>
}