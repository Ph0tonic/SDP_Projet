package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.dao.FirebaseHeatmapDao
import ch.epfl.sdp.database.dao.HeatmapDao
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import ch.epfl.sdp.database.repository.HeatmapRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer

class HeatmapManager {

    val heatmapRepository = HeatmapRepository()

    fun addMeasureToHeatmap(groupId: String, heatmapId: String, location: LatLng, intensity: Double) {
        val heatmaps = heatmapRepository.getGroupHeatmaps(groupId).value
        val heatmapData = if (heatmaps != null && heatmaps.containsKey(heatmapId)) {
            heatmaps[heatmapId]?.value?.copy()!!
        } else {
            HeatmapData(mutableListOf(), heatmapId)
        }
        heatmapData.dataPoints.add(HeatmapPointData(location, intensity))
        heatmapRepository.updateHeatmap(groupId, heatmapData)
    }

    fun getGroupHeatmaps(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        return heatmapRepository.getGroupHeatmaps(groupId)
    }

    fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
        heatmapRepository.removeAllHeatmapsOfSearchGroup(searchGroupId)
    }
}