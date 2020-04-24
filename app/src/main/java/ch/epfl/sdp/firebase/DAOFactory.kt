package ch.epfl.sdp.firebase

object DAOFactory {
    var instance : DAO = FirebaseDAO()
    fun setDAO(dao: DAO){
        instance = dao
    }
}