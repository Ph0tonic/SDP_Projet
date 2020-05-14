package ch.epfl.sdp.database.dao

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class FirebaseUserDao : UserDao {
    private var database: FirebaseDatabase = Firebase.database

    private val groupOperators: MutableMap<String, MutableLiveData<Set<UserData>>> = mutableMapOf()
    private val groupRescuers: MutableMap<String, MutableLiveData<Set<UserData>>> = mutableMapOf()

    override fun getUsersOfGroupWithRole(groupId: String, role: Role): MutableLiveData<Set<UserData>> {
        Log.w("FIREBASE", "get rescuers called")
        val mapData = if (role == Role.OPERATOR) groupOperators else groupRescuers

        if (!mapData.containsKey(groupId)) {
            //Initialise data
            val myRef = database.getReference("users/$groupId")
            mapData[groupId] = MutableLiveData(setOf())

            myRef.addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Timber.w("Failed to read value.")
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    TODO("A rescuer has changed, no action implemented")
                }

                override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                    val user = dataSnapshot.getValue(UserData::class.java)!!
                    Log.w("FIREBASE", "$user")
                    user.googleId = dataSnapshot.key!!
                    mapData[groupId]!!.value = mapData[groupId]!!.value!!.plus(user)
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(UserData::class.java)!!
                    user.email = dataSnapshot.key!!
                    mapData[groupId]!!.value = mapData[groupId]!!.value!!.minus(user)
                }
            })
        }
        return mapData[groupId]!!
    }
}