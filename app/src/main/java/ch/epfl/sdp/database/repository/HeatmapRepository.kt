package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseHeatmapDao
import ch.epfl.sdp.database.dao.HeatmapDao
import ch.epfl.sdp.database.data.HeatmapData

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

    fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        return dao.getHeatmapsOfSearchGroup(groupId)
    }

    fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
        dao.removeAllHeatmapsOfSearchGroup(searchGroupId)
    }
}