package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData

/**
 * Working local DAO
 */
class MockMarkerDao : MarkerDao {

    private val data: MutableMap<String, MutableLiveData<Set<MarkerData>>> = mutableMapOf()

    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        if (!data.containsKey(groupId)) {
            data[groupId] = MutableLiveData(setOf())
        }
        return data[groupId]!!
    }

    override fun addMarker(groupId: String, markerData: MarkerData) {
        if (!data.containsKey(groupId)) {
            data[groupId] = MutableLiveData<Set<MarkerData>>(setOf())
        }
        data[groupId]!!.value = data[groupId]!!.value!! + setOf(markerData)
    }

    override fun removeMarker(groupId: String, markerId: String) {
        if (!data.containsKey(groupId)) {
            data[groupId] = MutableLiveData(setOf())
        }
        data[groupId]!!.value = data[groupId]!!.value!!.filter { it.uuid != markerId }.toSet()
    }
}