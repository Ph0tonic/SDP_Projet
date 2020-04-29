package ch.epfl.sdp.firebase.data

import com.google.firebase.database.Exclude
import com.mapbox.mapboxsdk.geometry.LatLng

data class MarkerData(
        var location: LatLng? = null,
        @get:Exclude
        var uuid: String? = null)