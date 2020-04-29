package ch.epfl.sdp.database.dao

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseMarkersDao : MarkersDao {
    private var database: FirebaseDatabase = Firebase.database

    private val groupMarkers: MutableMap<String, MutableLiveData<Set<MarkerData>>> = mutableMapOf()

    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        if (!groupMarkers.containsKey(groupId)) {
            val myRef = database.getReference("markers/$groupId")
            groupMarkers[groupId] = MutableLiveData(setOf())
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.w("FIREBASE", dataSnapshot.toString())
                    val markers = dataSnapshot.children.map {
                        val marker = it.getValue(MarkerData::class.java)!!
                        marker.uuid = it.key
                        marker
                    }.toSet()
                    groupMarkers[groupId]!!.value = markers
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("FIREBASE", "Failed to read markers of search group from firebase.", error.toException())
                }
            })
        }
        return groupMarkers[groupId]!!
    }

    override fun addMarker(groupId: String, markerData: MarkerData) {
        database.getReference("markers/$groupId").push().setValue(markerData)
    }

    override fun removeMarker(groupId: String, markerId: String) {
        database.getReference("markers/$groupId/$markerId").removeValue()
    }
}
