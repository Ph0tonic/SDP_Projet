package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.HeatmapDao
import ch.epfl.sdp.firebase.data.HeatmapData

class HeatmapDataRepository(private val heatmapDao: HeatmapDao) {
    fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
        heatmapDao.updateHeatmap(groupId, heatmapData)
    }
    fun getGroupHeatmaps(groupId: String): MutableLiveData<MutableMap<String, HeatmapData>> {
        return heatmapDao.getHeatmapsOfSearchGroup(groupId)
    }
}