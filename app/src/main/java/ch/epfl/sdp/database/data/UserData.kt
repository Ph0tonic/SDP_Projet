package ch.epfl.sdp.database.data

import com.google.firebase.database.Exclude

data class UserData(
        @get:Exclude
        var googleId: String? = null,
        var email: String = ""
)