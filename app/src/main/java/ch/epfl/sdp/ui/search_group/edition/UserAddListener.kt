package ch.epfl.sdp.ui.search_group.edition

import ch.epfl.sdp.database.data.Role

interface UserAddListener {
    fun addUser(name: String, role: Role)
}