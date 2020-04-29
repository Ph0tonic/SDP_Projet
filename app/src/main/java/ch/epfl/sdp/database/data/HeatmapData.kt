package ch.epfl.sdp.database.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class HeatmapData(
        @get:PropertyName("data_points")
        @set:PropertyName("data_points")
        var dataPoints: MutableList<HeatmapPointData> = mutableListOf(),

        @get:Exclude
        var uuid: String? = null
) {
//    override fun equals(other: Any?): Boolean {
//        if (other !is HeatmapData) return false
//        return uuid == other.uuid && dataPoints == other.dataPoints
//    }
//
//    override fun hashCode(): Int {
//        return dataPoints.hashCode()
//    }
}