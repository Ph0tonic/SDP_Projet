package ch.epfl.sdp.firebase.data

import com.google.firebase.database.PropertyName
import com.mapbox.mapboxsdk.geometry.LatLng

data class HeatmapData(
        @get:PropertyName("data_points")
        @set:PropertyName("data_points")
        var dataPoints: MutableList<Pair<LatLng, Double>> = mutableListOf()
)