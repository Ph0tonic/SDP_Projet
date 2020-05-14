package ch.epfl.sdp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseHeatmapDao
import ch.epfl.sdp.database.dao.HeatmapDao
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import com.mapbox.mapboxsdk.geometry.LatLng

class HeatmapRepository {

    companion object {
        val DEFAULT_DAO = { FirebaseHeatmapDao() }

        // Change this for dependency injection
        var daoProvider: () -> HeatmapDao = DEFAULT_DAO
    }

    private val dao: HeatmapDao = daoProvider()

    fun addMeasureToHeatmap(groupId: String, heatmapId: String, location: LatLng, intensity: Double) {
        val heatmaps = dao.getHeatmapsOfSearchGroup(groupId).value
        val heatmapData = if (heatmaps != null && heatmaps.containsKey(heatmapId)) {
            heatmaps[heatmapId]?.value?.copy()!!
        } else {
            HeatmapData(mutableListOf(), heatmapId)
        }
        heatmapData.dataPoints.add(HeatmapPointData(location, intensity))
        dao.updateHeatmap(groupId, heatmapData)
    }

    fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        return dao.getHeatmapsOfSearchGroup(groupId)
    }

    fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
        dao.removeAllHeatmapsOfSearchGroup(searchGroupId)
    }
}