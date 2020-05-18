package ch.epfl.sdp.database.dao

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.MarkerData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseMarkersDao : MarkerDao {

    companion object {
        private const val ROOT_PATH = "markers"
    }

    private var database: FirebaseDatabase = Firebase.database

    private val groupMarkers: MutableMap<String, MutableLiveData<Set<MarkerData>>> = mutableMapOf()

    override fun getMarkersOfSearchGroup(groupId: String): MutableLiveData<Set<MarkerData>> {
        if (!groupMarkers.containsKey(groupId)) {
            //Initialise data
            val myRef = database.getReference("$ROOT_PATH/$groupId")
            groupMarkers[groupId] = MutableLiveData(setOf())

            myRef.addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.w("Firebase", "Failed to read value.")
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    throw IllegalAccessException("A marker has changed and this shouldn't happen !!!")
                }

                override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                    val marker = dataSnapshot.getValue(MarkerData::class.java)!!
                    marker.uuid = dataSnapshot.key
                    groupMarkers[groupId]!!.value = groupMarkers[groupId]!!.value!!.plus(marker)
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val marker = dataSnapshot.getValue(MarkerData::class.java)!!
                    marker.uuid = dataSnapshot.key
                    groupMarkers[groupId]!!.value = groupMarkers[groupId]!!.value!!.minus(marker)
                }
            })
        }
        return groupMarkers[groupId]!!
    }

    override fun addMarker(groupId: String, markerData: MarkerData) {
        database.getReference("$ROOT_PATH/$groupId").push().setValue(markerData)
    }

    override fun removeMarker(groupId: String, markerId: String) {
        database.getReference("$ROOT_PATH/$groupId").child(markerId).removeValue()
    }

    override fun removeAllMarkersOfSearchGroup(searchGroupId: String) {
        database.getReference("$ROOT_PATH/${searchGroupId}").removeValue()
    }
}
