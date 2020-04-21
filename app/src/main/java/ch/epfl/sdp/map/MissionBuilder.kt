package ch.epfl.sdp.map

import ch.epfl.sdp.drone.OverflightStrategy
import ch.epfl.sdp.drone.SimpleMultiPassOnQuadrilateral
import ch.epfl.sdp.searchareabuilder.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.properties.Delegates

class MissionBuilder {
    val generatedMissionChanged = mutableListOf<(List<LatLng>?) -> Unit>()

    private val eventChanged: (Any?, Any?, Any?) -> Unit = { _, _, _ ->
        val res = try {
            build()
        } catch (e: IllegalArgumentException) {
            null
        }
        generatedMissionChanged.forEach { it(res) }
    }

    private var startingLocation: LatLng? by Delegates.observable(null, eventChanged)
    private var searchArea: SearchArea? by Delegates.observable(null, eventChanged)
    private var overflightStrategy: OverflightStrategy by Delegates.observable(SimpleMultiPassOnQuadrilateral(SimpleMultiPassOnQuadrilateral.DEFAULT_DIST_BETWEEN_LINES), eventChanged)

    fun withStrategy(strategy: OverflightStrategy): MissionBuilder {
        this.overflightStrategy = strategy
        return this
    }

    fun withSearchArea(searchArea: SearchArea?): MissionBuilder {
        this.searchArea = searchArea
        return this
    }

    fun withStartingLocation(startingLocation: LatLng): MissionBuilder {
        this.startingLocation = startingLocation
        return this
    }

    fun build(): List<LatLng> {
        require(searchArea != null) { "Invalid search area" }
        require(startingLocation != null) { "Invalid starting location" }
        require(overflightStrategy.acceptArea(searchArea!!)) { "This strategy doesn't accept this search area" }
        return overflightStrategy.createFlightPath(startingLocation!!, searchArea!!)
    }
}
