package ch.epfl.sdp.database.data

import com.google.firebase.database.Exclude

data class SearchGroupData(
        @get:Exclude
        var uuid: String? = null,
        var name: String = "")