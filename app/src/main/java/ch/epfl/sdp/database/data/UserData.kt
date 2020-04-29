package ch.epfl.sdp.database.data

import com.google.firebase.database.PropertyName

data class UserData(
        @get:PropertyName("google_id")
        @set:PropertyName("google_id")
        var googleId: String = ""
)