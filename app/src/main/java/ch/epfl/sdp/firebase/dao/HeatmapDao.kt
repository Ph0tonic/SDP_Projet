package ch.epfl.sdp.firebase.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.data.HeatmapData

interface HeatmapDao {

    fun updateHeatmap(groupId: String, heatmapData: HeatmapData)
    fun getHeatmapsOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, HeatmapData>>
}