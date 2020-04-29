package ch.epfl.sdp.firebase.repository

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.FirebaseHeatmapDao
import ch.epfl.sdp.firebase.dao.HeatmapDao
import ch.epfl.sdp.firebase.data.HeatmapData

class HeatmapRepository {

    companion object {
        val DEFAULT_DAO = { FirebaseHeatmapDao() }

        // Change this for dependency injection
        var daoProvider: () -> HeatmapDao = DEFAULT_DAO
    }

    private val dao: HeatmapDao = daoProvider()

    fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
        dao.updateHeatmap(groupId, heatmapData)
    }

    fun getGroupHeatmaps(groupId: String): MutableLiveData<MutableMap<String, HeatmapData>> {
        return dao.getHeatmapsOfSearchGroup(groupId)
    }
}