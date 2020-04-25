package ch.epfl.sdp.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng

@IgnoreExtraProperties
data class Test(
    var field1: Int? = 0,
    var field2: String? = "",
    var field3: TestNested? = null,
    var field4: LatLng? = null
)

data class TestNested(
    var field1: Int? = 0,
    var field2: String? = ""
)

data class SearchGroup(
    var name: String? = null,
    var base_location: LatLng? = null,
    var search_location: LatLng? = null
)

class FirebaseDAO : DAO {
    private lateinit var database: FirebaseDatabase
    private var groups: MutableLiveData<List<String>> = MutableLiveData()

    override fun connect(): DAO {
        database = Firebase.database
        return this
    }

    override fun getGroups() {
        val myRef = database.getReference("Test")
        Log.w("FIREBASE", "Listener")
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                val g = mutableListOf<DataSnapshot>()
//                val res = dataSnapshot.children.toCollection(g)
                val data = dataSnapshot.getValue<Test>()
                Log.w("FIREBASE", "${data}")

//                g.forEach {
//                }
//                groups.pos/tValue(listOf(dataSnapshot.getValue<String>()) as List<String>)
                //val value = dataSnapshot.getValue<String>()
//                Log.w("FIREBASE", "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException())
            }
        })

        //TODO("Not yet implemented")
    }

    override fun joinGroupAsRescuer() {
        TODO("Not yet implemented")
    }

    override fun joinGroupAsOperator() {
        TODO("Not yet implemented")
    }

    override fun createGroup() {
        TODO("Not yet implemented")
    }

    override fun addPointToHeatmap() {
        TODO("Not yet implemented")
    }

    override fun leaveGroup() {
        TODO("Not yet implemented")
    }
}