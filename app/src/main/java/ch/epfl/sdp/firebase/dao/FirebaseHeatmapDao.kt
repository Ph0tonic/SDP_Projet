package ch.epfl.sdp.firebase.dao

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.Auth
import ch.epfl.sdp.firebase.data.HeatmapData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseHeatmapDao : HeatmapDao {

    private var database: FirebaseDatabase = Firebase.database

    // group_id / user_id / heatmap_data
    val groupHeatmaps: MutableMap<String, MutableLiveData<MutableMap<String,HeatmapData>>> = mutableMapOf()

    override fun updateHeatmap(groupId: String, heatmapData: HeatmapData) {
        val myRef = database.getReference("heatmaps/$groupId/${Auth.accountId.value!!}")
                .setValue(heatmapData)
    }

    override fun getHeatmapsOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, HeatmapData>> {
        if (!groupHeatmaps.containsKey(groupId)) {
            val myRef = database.getReference("heatmaps/$groupId")
            groupHeatmaps[groupId]= MutableLiveData(mutableMapOf())
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val heatmaps = dataSnapshot.children.associate { Pair(it.key!!, it.getValue(HeatmapData::class.java)!!)}.toMutableMap()
                    groupHeatmaps[groupId]!!.value = heatmaps
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("FIREBASE", "Failed to read value.", error.toException())
                }
            })
        }
        return groupHeatmaps[groupId]!!
    }
}