package ch.epfl.sdp.firebase.data

import com.google.firebase.database.PropertyName

data class User(
        @get:PropertyName("google_id")
        @set:PropertyName("google_id")
        var googleId: String = ""
)