package ch.epfl.sdp.database.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData

/**
 * Working local DAO
 */
class MockHeatmapDao : HeatmapDao {

    val data: MutableMap<String, MutableLiveData<MutableMap<String, MutableLiveData<HeatmapData>>>> = mutableMapOf()

    override fun getHeatmapsOfSearchGroup(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        if (!data.containsKey(groupId)) {
            data[groupId] = MutableLiveData(mutableMapOf())
        }
        return data[groupId]!!
    }

    override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
        if(!data.containsKey(groupId)){
            data[groupId] = MutableLiveData<MutableMap<String, MutableLiveData<HeatmapData>>>(mutableMapOf())
        }
        if (!data[groupId]!!.value!!.containsKey(heatmapData.uuid!!)) {
            data[groupId]!!.value!![heatmapData.uuid!!] = MutableLiveData(heatmapData)
        } else {
            data[groupId]!!.value!![heatmapData.uuid!!]!!.value = heatmapData
        }
    }

    override fun removeAllHeatmapsOfSearchGroup(groupId: String) {
        data[groupId]?.value = mutableMapOf()
        data.remove(groupId)
    }
}