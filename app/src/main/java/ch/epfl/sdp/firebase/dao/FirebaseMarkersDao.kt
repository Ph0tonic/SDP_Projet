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

    private val markersGroups: MutableMap<String, MutableLiveData<MutableMap<String, LatLng>>> = mutableMapOf()

    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<MutableMap<String, LatLng>> {
        if (!markersGroups.containsKey(groupId)) {
            val myRef = database.getReference("markers/$groupId")
            markersGroups[groupId] = MutableLiveData<MutableMap<String, LatLng>>(mutableMapOf())

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val markers = mutableMapOf<String, LatLng>()
                    dataSnapshot.children.forEach { c ->
                        markers[c.key!!] = c.getValue(LatLng::class.java)!!
                    }
                    markersGroups[groupId]?.value = markers
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("FIREBASE", "Failed to read value.", error.toException())
                }
            })
        }
        return markersGroups[groupId]!!
    }
}
