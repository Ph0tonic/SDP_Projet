package ch.epfl.sdp.database.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class FirebaseHeatmapDao : HeatmapDao {

    companion object {
        private const val ROOT_PATH = "heatmaps"
    }

    private var database: FirebaseDatabase = Firebase.database

    // group_id / user_id / heatmap_data
    val groupHeatmaps: MutableMap<String, MutableLiveData<MutableMap<String, MutableLiveData<HeatmapData>>>> = mutableMapOf()

    override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
        database.getReference("$ROOT_PATH/$groupId/${heatmapData.uuid}")
                .setValue(heatmapData)
    }

    override fun removeAllHeatmapsOfSearchGroup(searchGroupId: String) {
        database.getReference("$ROOT_PATH/${searchGroupId}").removeValue()
    }

    override fun getHeatmapsOfSearchGroup(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        if (!groupHeatmaps.containsKey(groupId)) {
            val myRef = database.getReference("$ROOT_PATH/$groupId")
            groupHeatmaps[groupId] = MutableLiveData(mutableMapOf())

            myRef.addChildEventListener(object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.w("Failed to read heatmaps of search group from firebase.")
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    val newHeatmapData = dataSnapshot.getValue(HeatmapData::class.java)!!
                    newHeatmapData.uuid = dataSnapshot.key
                    groupHeatmaps[groupId]!!.value!![dataSnapshot.key!!]!!.value = newHeatmapData
                }

                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    val newHeatmapData = dataSnapshot.getValue(HeatmapData::class.java)!!
                    newHeatmapData.uuid = dataSnapshot.key
                    groupHeatmaps[groupId]!!.value!![dataSnapshot.key!!] = MutableLiveData(newHeatmapData)

                    // Trigger parent live data
                    groupHeatmaps[groupId]!!.value = groupHeatmaps[groupId]!!.value
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    groupHeatmaps[groupId]!!.value!!.remove(dataSnapshot.key)
                    groupHeatmaps[groupId]!!.value = groupHeatmaps[groupId]!!.value
                }
            })
        }
        return groupHeatmaps[groupId]!!
    }
}