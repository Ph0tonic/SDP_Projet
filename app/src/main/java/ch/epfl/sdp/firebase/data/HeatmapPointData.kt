package ch.epfl.sdp.firebase.data

import com.mapbox.mapboxsdk.geometry.LatLng

class HeatmapPointData(
        var position: LatLng? = null,
        var intensity: Double = 0.0
)