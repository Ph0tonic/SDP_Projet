package ch.epfl.sdp.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseDAO : DAO {
    private lateinit var database: DatabaseReference

    override fun connect() {
        database = Firebase.database.reference
    }

    override fun getGroups() {
        TODO("Not yet implemented")
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