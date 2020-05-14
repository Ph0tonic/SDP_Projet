package ch.epfl.sdp.database.data

import ch.epfl.sdp.utils.enumStringLowerCase
import com.google.firebase.database.Exclude

data class UserData(
        var email: String = "",
        @get:Exclude
        var googleId: String? = null,
        @Exclude
        var roleEnum: Role = Role.RESCUER) {
    var role: String by enumStringLowerCase(UserData::roleEnum)
}