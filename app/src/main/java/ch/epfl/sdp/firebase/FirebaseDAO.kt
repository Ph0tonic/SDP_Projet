package ch.epfl.sdp.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import timber.log.Timber


class FirebaseDAO : DAO {
    private lateinit var database: FirebaseDatabase
    private var groups : MutableLiveData<List<String>> = MutableLiveData()

    override fun connect(): DAO {
        database = Firebase.database
        return this
    }

    override fun getGroups() {
        val myRef = database.getReference("test")

        Log.w("FIREBASE","Listener")
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                groups.postValue(listOf(dataSnapshot.getValue<String>()) as List<String>)
                val value = dataSnapshot.getValue<String>()
                Log.w("FIREBASE","Value is: $value")
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