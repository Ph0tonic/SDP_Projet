package ch.epfl.sdp.database.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.mapbox.mapboxsdk.geometry.LatLng

data class SearchGroupData(
        @get:Exclude
        var uuid: String? = null,
        var name: String = "",

        @get:PropertyName("base_location")
        @set:PropertyName("base_location")
        var baseLocation: LatLng? = null,

        @get:PropertyName("search_location")
        @set:PropertyName("search_location")
        var searchLocation: LatLng? = null)