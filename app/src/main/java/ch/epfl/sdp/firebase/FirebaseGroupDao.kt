package ch.epfl.sdp.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseGroupDao : GroupDao {
    private var database: FirebaseDatabase = Firebase.database

    private val groups: MutableLiveData<List<SearchGroup>> = MutableLiveData(mutableListOf())

    init {
        val myRef = database.getReference("search_groups")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groups.value = (dataSnapshot.children.map { c ->
                    // Get group data (without key)
                    val group = c.getValue(SearchGroup::class.java)
                    // Retrieve group key and set it
                    group?.uuid = c.key
                    group!!
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException())
            }
        })
    }

    override fun getGroups(): MutableLiveData<List<SearchGroup>> {
        return groups
    }
}