package ch.epfl.sdp.firebase

import com.google.firebase.database.PropertyName
import com.mapbox.mapboxsdk.geometry.LatLng

data class SearchGroup(
        var uuid: String? = null,

        @get:PropertyName("base_location")
        @set:PropertyName("base_location")
        var baseLocation: LatLng? = null,

        var name: String = "",

        @get:PropertyName("search_location")
        @set:PropertyName("search_location")
        var search_location:LatLng? = null){
}