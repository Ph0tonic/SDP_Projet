package ch.epfl.sdp.firebase.dao

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.data.SearchGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseGroupDao : GroupDao {
    private var database: FirebaseDatabase = Firebase.database

    private val groups: MutableLiveData<List<SearchGroup>> = MutableLiveData(mutableListOf())
    private val onGroupChangedListeners: MutableMap<String,MutableList<OnGroupChangedListener>> = mutableMapOf()

    init {
        val myRef = database.getReference("search_groups")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groups.value = (dataSnapshot.children.map { c ->
                    // Get group data (without key)
                    val group = c.getValue(SearchGroup::class.java)
                    // Retrieve group key generated by google and use it
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun addOnGroupChangedListener(OnGroupChangedListener: OnGroupChangedListener, groupId: String){
        val myRef = database.getReference("search_groups/$groupId")
        onGroupChangedListeners.putIfAbsent(groupId, mutableListOf())!!.add(OnGroupChangedListener)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                OnGroupChangedListener.onGroupChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException())
            }
        })
    }

    override fun removeGroupListeners(groupId: String){
        onGroupChangedListeners.remove(groupId)
    }
}