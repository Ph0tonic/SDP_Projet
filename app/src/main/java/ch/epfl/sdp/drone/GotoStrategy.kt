package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng

class GotoStrategy: OverflightStrategy {
    companion object Constraints {
        const val pinPointsAmount = 1
    }

    override fun createFlightPath(pinpoints: List<LatLng>): List<LatLng> {
        require(pinpoints.size == pinPointsAmount) {
            "This strategy requires exactly $pinPointsAmount pinpoints, ${pinpoints.size} given."
        }

        return pinpoints
    }
}