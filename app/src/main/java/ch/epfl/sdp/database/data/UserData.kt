package ch.epfl.sdp.database.data

import ch.epfl.sdp.utils.enumStringLowerCase
import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class UserData(
        var email: String = "",
        @get:Exclude
        var googleId: String? = null,
        @get:Exclude
        @set:Exclude
        var role: Role = Role.RESCUER) {

    @get:PropertyName("role")
    @set:PropertyName("role")
    var roleString: String by enumStringLowerCase(UserData::role)
}