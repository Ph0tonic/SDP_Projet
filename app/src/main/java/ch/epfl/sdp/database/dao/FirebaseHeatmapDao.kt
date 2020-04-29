package ch.epfl.sdp.database.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.Auth
import ch.epfl.sdp.database.data.HeatmapData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseHeatmapDao : HeatmapDao {

    private var database: FirebaseDatabase = Firebase.database

    // group_id / user_id / heatmap_data
    val groupHeatmaps: MutableMap<String, MutableLiveData<MutableMap<String, MutableLiveData<HeatmapData>>>> = mutableMapOf()

    override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
        database.getReference("heatmaps/$groupId/${Auth.accountId.value!!}")
                .setValue(heatmapData)
    }

    override fun getHeatmapsOfSearchGroup(groupId: String): LiveData<MutableMap<String, MutableLiveData<HeatmapData>>> {
        if (!groupHeatmaps.containsKey(groupId)) {
            val myRef = database.getReference("heatmaps/$groupId")
            groupHeatmaps[groupId] = MutableLiveData(mutableMapOf())

            myRef.addChildEventListener(object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.w("FIREBASE/HEATMAP", "Failed to read heatmaps of search group from firebase.", error.toException())
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.w("FIREBASE/HEATMAP", "Updating heatmap")
                    val newHeatmapData = dataSnapshot.getValue(HeatmapData::class.java)!!
                    newHeatmapData.uuid = dataSnapshot.key
                    groupHeatmaps[groupId]!!.value!![dataSnapshot.key!!]!!.value = newHeatmapData
                }

                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.w("FIREBASE/HEATMAP", "Adding new heatmap groupId: $groupId")
                    val newHeatmapData = dataSnapshot.getValue(HeatmapData::class.java)!!
                    newHeatmapData.uuid = dataSnapshot.key
                    groupHeatmaps[groupId]!!.value!![dataSnapshot.key!!] = MutableLiveData(newHeatmapData)

                    // Trigger parent live data
                    groupHeatmaps[groupId]!!.value = groupHeatmaps[groupId]!!.value
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

            })

//            myRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    val heatmaps = dataSnapshot.children.map {
//                        val heatmapData = it.getValue(HeatmapData::class.java)!!
//                        heatmapData.uuid = it.key!!
//                        heatmapData
//                    }
//                    groupHeatmaps[groupId]!!.value = heatmaps
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.w("FIREBASE", "Failed to read heatmap from firebase.", error.toException())
//                }
//            })
        }
        return groupHeatmaps[groupId]!!
    }
}