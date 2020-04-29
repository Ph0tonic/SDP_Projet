package ch.epfl.sdp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.SimpleMultiPassOnQuadrilateral
import ch.epfl.sdp.firebase.data.HeatmapData
import ch.epfl.sdp.firebase.repository.MarkerRepository
import ch.epfl.sdp.firebase.repository.HeatmapRepository
import ch.epfl.sdp.map.*
import ch.epfl.sdp.ui.maps.MapUtils
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : MapViewBaseActivity(), OnMapReadyCallback {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var heatmap: Heatmap

    private var isMapReady = false

    private lateinit var heatmapGeoJsonSource: GeoJsonSource
    private lateinit var victimSymbolManager: SymbolManager

    private lateinit var droneBatteryLevelImageView: ImageView
    private lateinit var droneBatteryLevelTextView: TextView
    private lateinit var distanceToUserTextView: TextView
    private lateinit var userLongitudeTextView: TextView
    private lateinit var droneAltitudeTextView: TextView
    private lateinit var userLatitudeTextView: TextView
    private lateinit var droneSpeedTextView: TextView

    private var victimSymbolLongClickConsumed = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val victimMarkers = mutableMapOf<String, Symbol>()

    /** Builders */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var searchAreaBuilder: SearchAreaBuilder

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var missionBuilder: MissionBuilder

    /** Painters */
    private lateinit var searchAreaPainter: MapboxSearchAreaPainter
    private lateinit var missionPainter: MapboxMissionPainter
    private lateinit var dronePainter: MapboxDronePainter
    private lateinit var userPainter: MapboxUserPainter

    private val droneBatteryLevelDrawables = listOf(
            Pair(.0, R.drawable.ic_battery1),
            Pair(.05, R.drawable.ic_battery2),
            Pair(.23, R.drawable.ic_battery3),
            Pair(.41, R.drawable.ic_battery4),
            Pair(.59, R.drawable.ic_battery5),
            Pair(.77, R.drawable.ic_battery6),
            Pair(.95, R.drawable.ic_battery7)
    )

    private var dronePositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let { updateDronePosition(it); if (::dronePainter.isInitialized) dronePainter.paint(it) }
    }
    private var userPositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let { updateUserPosition(it); if (::userPainter.isInitialized) userPainter.paint(it) }
    }
    private var droneBatteryObserver = Observer<Float> { newBatteryLevel: Float? ->

        // Always update the text string
        updateTextView(droneBatteryLevelTextView, newBatteryLevel?.times(100)?.toDouble(), PERCENTAGE_FORMAT)

        // Only update the icon if the battery level is not null
        newBatteryLevel?.let {
            val newBatteryDrawable = droneBatteryLevelDrawables
                    .filter { x -> x.first <= newBatteryLevel.coerceAtLeast(0f) }
                    .maxBy { x -> x.first }!!
                    .second
            droneBatteryLevelImageView.setImageResource(newBatteryDrawable)
            droneBatteryLevelImageView.tag = newBatteryDrawable
        }
    }
    private var droneAltitudeObserver = Observer<Float> { newAltitude: Float? ->
        updateTextView(droneAltitudeTextView, newAltitude?.toDouble(), DISTANCE_FORMAT)
    }
    private var droneSpeedObserver = Observer<Float> { newSpeed: Float? ->
        updateTextView(droneSpeedTextView, newSpeed?.toDouble(), SPEED_FORMAT)
    }

    val markerRepository = MarkerRepository()
    val heatmapRepository = HeatmapRepository()

    companion object {
        const val MAP_NOT_READY_DESCRIPTION: String = "MAP NOT READY"
        const val MAP_READY_DESCRIPTION: String = "MAP READY"

        const val ID_ICON_VICTIM: String = "airport"

        private const val DISTANCE_FORMAT = " %.1f m"
        private const val PERCENTAGE_FORMAT = " %.0f%%"
        private const val SPEED_FORMAT = " %.1f m/s"
        private const val COORDINATE_FORMAT = " %.7f"

        private const val DEFAULT_NEW_MARKER_ID = "new_marker_id"
        private const val VICTIM_MARKER_ID_PROPERTY_NAME = "id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        droneBatteryLevelTextView = findViewById(R.id.battery_level)
        droneAltitudeTextView = findViewById(R.id.altitude)
        distanceToUserTextView = findViewById(R.id.distance_to_user)
        droneSpeedTextView = findViewById(R.id.speed)

        //TODO: Give user location if current drone position is not available
        droneBatteryLevelImageView = findViewById(R.id.battery_level_icon)

        findViewById<Button>(R.id.start_mission_button).setOnClickListener {
            Drone.startMission(DroneMission.makeDroneMission(
                    missionBuilder.build()
            ).getMissionItems())
        }
        findViewById<Button>(R.id.stored_offline_map).setOnClickListener {
            startActivity(Intent(applicationContext, OfflineManagerActivity::class.java))
        }
        findViewById<Button>(R.id.clear_waypoints).setOnClickListener {
            clearWaypoints()
        }

        userLatitudeTextView = findViewById(R.id.tv_latitude)
        userLongitudeTextView = findViewById(R.id.tv_longitude)

        mapView.contentDescription = MAP_NOT_READY_DESCRIPTION

        CentralLocationManager.configure(this)
    }

    override fun onResume() {
        super.onResume()
        Drone.currentPositionLiveData.observe(this, dronePositionObserver)
        Drone.currentBatteryLevelLiveData.observe(this, droneBatteryObserver)
        Drone.currentAbsoluteAltitudeLiveData.observe(this, droneAltitudeObserver)
        Drone.currentSpeedLiveData.observe(this, droneSpeedObserver)
        CentralLocationManager.currentUserPosition.observe(this, userPositionObserver)
    }

    override fun onPause() {
        super.onPause()
        CentralLocationManager.currentUserPosition.removeObserver(userPositionObserver)
        Drone.currentPositionLiveData.removeObserver(dronePositionObserver)
        Drone.currentBatteryLevelLiveData.removeObserver(droneSpeedObserver)
        Drone.currentAbsoluteAltitudeLiveData.removeObserver(droneAltitudeObserver)
        Drone.currentSpeedLiveData.removeObserver(droneSpeedObserver)

        if (isMapReady) MapUtils.saveCameraPositionAndZoomToPrefs(mapboxMap.cameraPosition)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        //TODO change this to create heatmap via repo
        this.heatmap = Heatmap(MutableLiveData(HeatmapData()))

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            userPainter = MapboxUserPainter(mapView, mapboxMap, style)
            dronePainter = MapboxDronePainter(mapView, mapboxMap, style)
            missionPainter = MapboxMissionPainter(mapView, mapboxMap, style)
            searchAreaPainter = MapboxQuadrilateralPainter(mapView, mapboxMap, style)

            victimSymbolManager = SymbolManager(mapView, mapboxMap, style)
            victimSymbolManager.iconAllowOverlap = true
            victimSymbolManager.symbolSpacing = 0F
            victimSymbolManager.iconIgnorePlacement = true
            victimSymbolManager.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT

            victimSymbolManager.addLongClickListener {
                victimSymbolManager.delete(it)
                val markerId = it.data!!.asJsonObject.get(VICTIM_MARKER_ID_PROPERTY_NAME).asString
                victimMarkers.remove(markerId)
                //TODO replace g1 constant
                markerRepository.removeMarkerForSearchGroup("g1", markerId)
                victimSymbolLongClickConsumed = true
            }

            style.addImage(ID_ICON_VICTIM, getDrawable(R.drawable.ic_victim)!!)

            mapboxMap.addOnMapClickListener {
                onMapClicked(it)
                true
            }
            mapboxMap.addOnMapLongClickListener {
                onMapLongClicked(it)
                true
            }

            heatmapGeoJsonSource = GeoJsonSource(getString(R.string.heatmap_source_ID), GeoJsonOptions()
                    .withCluster(true)
                    .withClusterProperty("intensities", Expression.literal("+"), Expression.get("intensity"))
                    .withClusterMaxZoom(13)
            )

            heatmapGeoJsonSource.setGeoJson(heatmap.getFeatures())
            style.addSource(heatmapGeoJsonSource)

            MapUtils.createLayersForHeatMap(style)

            // Load latest location
            mapboxMap.cameraPosition = MapUtils.getLastCameraState()

            // Used to detect when the map is ready in tests
            mapView.contentDescription = MAP_READY_DESCRIPTION

            //Create builders
            missionBuilder = MissionBuilder()
                    .withStartingLocation(LatLng(MapUtils.DEFAULT_LATITUDE, MapUtils.DEFAULT_LONGITUDE))
                    .withStrategy(SimpleMultiPassOnQuadrilateral(Drone.GROUND_SENSOR_SCOPE))
            searchAreaBuilder = QuadrilateralBuilder()


            // Add listeners to builders
            searchAreaBuilder.searchAreaChanged.add { missionBuilder.withSearchArea(it) }
            searchAreaBuilder.verticesChanged.add { searchAreaPainter.paint(it) }
            missionBuilder.generatedMissionChanged.add { missionPainter.paint(it) }
            searchAreaPainter.onMoveVertex.add { old, new -> searchAreaBuilder.moveVertex(old, new) }

            // Location listener on drone
            Drone.currentPositionLiveData.observe(this, Observer { missionBuilder.withStartingLocation(it) })

            isMapReady = true
            onceMapReady()
        }
    }

    /**
     * Called once the map and the style are completely initialized
     */
    private fun onceMapReady() {
        //TODO replace g1 constant
        markerRepository.getMarkersOfSearchGroup("g1").observe(this, Observer { markers ->
            Log.w("FIREBASE", markers.toString())
            val removedMarkers = victimMarkers.keys - markers.map { it.uuid }
            removedMarkers.forEach {
                victimSymbolManager.delete(victimMarkers[it])
            }
            markers.filter { !victimMarkers.containsKey(it.uuid) }.forEach {
                addVictimMarker(it.location!!, it.uuid!!)
            }
        })

        /**Uncomment this to see a virtual heatmap, if uncommented, tests won't pass**/
        //addVirtualPointsToHeatmap()
    }

    /**
     * Updates the text of the given textView with the given value and format, or the default string
     * if the value is null
     */
    private fun updateTextView(textView: TextView, value: Double?, formatString: String) {
        textView.text = value?.let { formatString.format(it) } ?: getString(R.string.no_info)
    }

    /**
     * Map clic to current eventListener
     */
    fun onMapClicked(position: LatLng) {
        try {
            searchAreaBuilder.addVertex(position)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Map long click to current eventListener
     */
    private fun onMapLongClicked(position: LatLng) {
        if (!victimSymbolLongClickConsumed) {
            addVictimMarker(position)
            //TODO replace g1 constant
            markerRepository.addMarkerForSearchGroup("g1", position)
        }
        victimSymbolLongClickConsumed = false
    }

    /**
     * Clears the waypoints list and removes all the lines and points related to waypoints
     */
    private fun clearWaypoints() {
        if (!isMapReady) return
        searchAreaBuilder.reset()
    }

    /**
     * Adds a heat point to the heatmap
     */
    fun addPointToHeatMap(location: LatLng, intensity: Double) {
        if (!isMapReady) return
        /* Will be needed when we have the signal of the drone implemented */
        //feature.addNumberProperty("intensity", Drone.getSignalStrength())
        heatmap.addPoint(location, intensity)
        heatmapGeoJsonSource.setGeoJson(heatmap.getFeatures())
    }

    private fun addVictimMarker(latLng: LatLng, markerId: String = DEFAULT_NEW_MARKER_ID) {
        if (!isMapReady) return
        val markerProperties = JsonObject()
        markerProperties.addProperty(VICTIM_MARKER_ID_PROPERTY_NAME, markerId)
        val symbolOptions = SymbolOptions()
                .withLatLng(LatLng(latLng))
                .withIconImage(ID_ICON_VICTIM)
                .withData(markerProperties)
        victimMarkers[markerId] = victimSymbolManager.create(symbolOptions)
    }

    /**
     * Update [currentPositionMarker] position with a new [position].
     *
     * @param newLatLng new position of the vehicle
     */
    private fun updateDronePosition(newLatLng: LatLng) {
        CentralLocationManager.currentUserPosition.value?.let {
            updateTextView(distanceToUserTextView, it.distanceTo(newLatLng), DISTANCE_FORMAT)
        }
    }

    /**
     * Updates the user position if the drawing managers are ready
     */
    private fun updateUserPosition(userLatLng: LatLng) {
        updateTextView(userLatitudeTextView, userLatLng.latitude, getString(R.string.lat) + COORDINATE_FORMAT)
        updateTextView(userLongitudeTextView, userLatLng.longitude, getString(R.string.lon) + COORDINATE_FORMAT)

        Drone.currentPositionLiveData.value?.let {
            updateTextView(distanceToUserTextView, it.distanceTo(userLatLng), DISTANCE_FORMAT)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**THIS IS JUST TO ADD SOME POINTS, IT WILL BE REMOVED AFTERWARDS**/
//    private fun addVirtualPointsToHeatmap() {
//        val center = LatLng(47.3975, 8.5445)
//        //Drone.getSignalStrength={10.0}
//        //precision should be 0.00003
//        for (i in 0..10) {
//            for (j in 0..10) {
//                val point = LatLng(47.397 + j / 10000.0, 8.544 + i / 10000.0)
//                val intensity = 10 - 1 * (center.distanceTo(point) / 10.0 - 1.0)
//                addPointToHeatMap(point, intensity)
//            }
//        }
//    }
}
