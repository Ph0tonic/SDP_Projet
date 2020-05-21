package ch.epfl.sdp.mission

import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.searcharea.SearchArea
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
    private var overflightStrategy: OverflightStrategy by Delegates.observable(SimpleQuadStrategy(), eventChanged)

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
        require(searchArea != null) { "Search area cannot be null" }
        require(startingLocation != null) { "Starting location cannot be null" }
        require(overflightStrategy.acceptArea(searchArea!!)) { "This strategy doesn't accept this search area" }
        require(!areVerticesTooDistant(searchArea!!.vertices)) { "Points are too far from one another" }
        return overflightStrategy.createFlightPath(startingLocation!!, searchArea!!)
    }

    private fun areVerticesTooDistant(vertices: List<LatLng>): Boolean {
        return vertices.any { v1 -> vertices.any { v1.distanceTo(it) > Drone.MAX_DISTANCE_BETWEEN_POINTS_IN_AREA } }
    }
}
