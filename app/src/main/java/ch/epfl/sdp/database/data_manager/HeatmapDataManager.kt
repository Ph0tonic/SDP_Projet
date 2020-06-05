package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.IdentifierUtils
import com.mapbox.mapboxsdk.geometry.LatLng

class HeatmapDataManager {

    private val heatmapRepository = HeatmapRepositoryProvider.provide()

    fun addMeasureToHeatmap(groupId: String, location: LatLng, intensity: Double) {
        val heatmapId = Auth.accountId.value + "__" + IdentifierUtils.id()

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