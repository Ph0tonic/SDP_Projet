package ch.epfl.sdp.database.data

import com.mapbox.mapboxsdk.geometry.LatLng

data class HeatmapPointData(
        var position: LatLng? = null,
        var intensity: Double = 0.0
)