package ch.epfl.sdp.firebase.dao

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng

class FirebaseMarkersDao : MarkersDao {
    private var database: FirebaseDatabase = Firebase.database

    private val groupMarkers: MutableMap<String, MutableLiveData<MutableMap<String,LatLng>>> = mutableMapOf()

    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, LatLng>> {
        if (!groupMarkers.containsKey(groupId)) {
            val myRef = database.getReference("markers/$groupId")
            groupMarkers[groupId]= MutableLiveData(mutableMapOf())
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.w("FIREBASE: ",dataSnapshot.toString())
                    val markers = dataSnapshot.children.associate { Pair(it.key!!, it.getValue(LatLng::class.java)!!)}.toMutableMap()
                    groupMarkers[groupId]!!.value = markers
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("FIREBASE", "Failed to read value.", error.toException())
                }
            })
        }
        return groupMarkers[groupId]!!
    }
}
