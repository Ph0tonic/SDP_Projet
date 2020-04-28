package ch.epfl.sdp.firebase.data

import com.google.firebase.database.PropertyName

data class HeatmapData(
        @get:PropertyName("data_points")
        @set:PropertyName("data_points")
        var dataPoints: MutableList<HeatmapPointData> = mutableListOf()
)