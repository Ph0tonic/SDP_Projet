package ch.epfl.sdp.ui.maps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.DroneUtils
import ch.epfl.sdp.map.*
import ch.epfl.sdp.map.MapUtils.DEFAULT_ZOOM
import ch.epfl.sdp.map.MapUtils.ZOOM_TOLERANCE
import ch.epfl.sdp.mission.MissionBuilder
import ch.epfl.sdp.mission.OverflightStrategy
import ch.epfl.sdp.mission.SimpleMultiPassOnQuadrilateral
import ch.epfl.sdp.mission.SpiralStrategy
import ch.epfl.sdp.searcharea.CircleBuilder
import ch.epfl.sdp.searcharea.QuadrilateralBuilder
import ch.epfl.sdp.searcharea.SearchAreaBuilder
import ch.epfl.sdp.ui.maps.offline.OfflineManagerActivity
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.getbase.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT

/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : MapViewBaseActivity(), OnMapReadyCallback {

    private lateinit var groupId: String
    private var isMapReady = false

    private lateinit var mapboxMap: MapboxMap
    private lateinit var victimSymbolManager: SymbolManager

    private lateinit var snackbar: Snackbar

    private lateinit var droneBatteryLevelImageView: ImageView
    private lateinit var droneBatteryLevelTextView: TextView
    private lateinit var distanceToUserTextView: TextView
    private lateinit var droneAltitudeTextView: TextView
    private lateinit var droneSpeedTextView: TextView
    private lateinit var strategyPickerButton: FloatingActionButton

    private lateinit var role: Role
    private var currentStrategy: OverflightStrategy = SimpleMultiPassOnQuadrilateral(Drone.GROUND_SENSOR_SCOPE)

    private var victimSymbolLongClickConsumed = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val victimMarkers = mutableMapOf<String, Symbol>()

    /** Builders */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var searchAreaBuilder: SearchAreaBuilder

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var missionBuilder: MissionBuilder

    /* Repositories */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val heatmapRepository = HeatmapRepository()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val markerRepository = MarkerRepository()

    /* Painters */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val heatmapPainters = mutableMapOf<String, MapboxHeatmapPainter>()
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

    private var droneAltitudeObserver = Observer<Float> { newAltitude: Float? ->
        updateTextView(droneAltitudeTextView, newAltitude?.toDouble(), DISTANCE_FORMAT)
    }
    private var droneSpeedObserver = Observer<Float> { newSpeed: Float? ->
        updateTextView(droneSpeedTextView, newSpeed?.toDouble(), SPEED_FORMAT)
    }
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

    companion object {
        const val ID_ICON_VICTIM: String = "airport"

        private const val DISTANCE_FORMAT = " %.1f m"
        private const val PERCENTAGE_FORMAT = " %.0f%%"
        private const val SPEED_FORMAT = " %.1f m/s"
        private const val COORDINATE_FORMAT = " %.7f"

        private const val VICTIM_MARKER_ID_PROPERTY_NAME = "id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO move "groupId" to Strings
        requireNotNull(intent.getStringExtra(getString(R.string.intent_key_group_id))) { "MapActivity should be provided with a searchGroupId\n" }
        require(Auth.loggedIn.value == true) { "You need to be logged in to access MapActivity" }
        requireNotNull(Auth.accountId.value) { "You need to have an account ID set to access MapActivity" }
        requireNotNull(intent.getSerializableExtra(getString(R.string.intent_key_role))) { "MapActivity should be provided with a role" }

        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        groupId = intent.getStringExtra(getString(R.string.intent_key_group_id))!!
        role = intent.getSerializableExtra(getString(R.string.intent_key_role)) as Role

        droneBatteryLevelImageView = findViewById(R.id.battery_level_icon)
        droneBatteryLevelTextView = findViewById(R.id.battery_level)
        droneAltitudeTextView = findViewById(R.id.altitude)
        distanceToUserTextView = findViewById(R.id.distance_to_user)
        droneSpeedTextView = findViewById(R.id.speed)
        strategyPickerButton = findViewById(R.id.strategy_picker_button)
        snackbar = Snackbar.make(mapView, R.string.not_connected_message, Snackbar.LENGTH_LONG)

        if (role == Role.RESCUER) {
            findViewById<FloatingActionButton>(R.id.start_or_return_button)!!.visibility = View.GONE
            findViewById<FloatingActionButton>(R.id.clear_button)!!.visibility = View.GONE
            findViewById<FloatingActionButton>(R.id.locate_button)!!.visibility = View.GONE
            findViewById<FloatingActionButton>(R.id.strategy_picker_button)!!.visibility = View.GONE
            findViewById<LinearLayout>(R.id.switch_button)!!.visibility = View.GONE
            findViewById<TableLayout>(R.id.drone_status)!!.visibility = View.GONE
        }

        mapView.contentDescription = getString(R.string.map_not_ready)

        //TODO: Give user location if current drone position is not available
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

    override fun onDestroy() {
        super.onDestroy()
        if (isMapReady) {
            userPainter.onDestroy()
            dronePainter.onDestroy()
            missionPainter.onDestroy()
            victimSymbolManager.onDestroy()
            searchAreaPainter.onDestroy()
        }

        CentralLocationManager.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            userPainter = MapboxUserPainter(mapView, mapboxMap, style)
            dronePainter = MapboxDronePainter(mapView, mapboxMap, style)
            missionPainter = MapboxMissionPainter(mapView, mapboxMap, style)
            setupVictimSymbolManager(style)

            mapboxMap.addOnMapClickListener {
                onMapClicked(it)
                true
            }
            mapboxMap.addOnMapLongClickListener {
                onMapLongClicked(it)
                true
            }

            // Load latest location
            mapboxMap.cameraPosition = MapUtils.getLastCameraState()

            //Create builders
            missionBuilder = MissionBuilder()
                    .withStartingLocation(LatLng(MapUtils.DEFAULT_LATITUDE, MapUtils.DEFAULT_LONGITUDE))

            setStrategy(SimpleMultiPassOnQuadrilateral(Drone.GROUND_SENSOR_SCOPE))

            // Add listeners to builders
            missionBuilder.generatedMissionChanged.add { missionPainter.paint(it) }

            // Location listener on drone
            Drone.currentPositionLiveData.observe(this, Observer { missionBuilder.withStartingLocation(it) })

            isMapReady = true
            onceMapReady(style)

            // Used to detect when the map is ready in tests
            mapView.contentDescription = getString(R.string.map_ready)
        }
    }

    private fun setupVictimSymbolManager(style: Style) {
        victimSymbolManager = SymbolManager(mapView, mapboxMap, style)

        victimSymbolManager.iconAllowOverlap = true
        victimSymbolManager.symbolSpacing = 0F
        victimSymbolManager.iconIgnorePlacement = true
        victimSymbolManager.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT

        victimSymbolManager.addLongClickListener {
            val markerId = it.data!!.asJsonObject.get(VICTIM_MARKER_ID_PROPERTY_NAME).asString
            markerRepository.removeMarkerForSearchGroup(groupId, markerId)
            victimSymbolLongClickConsumed = true
        }

        style.addImage(ID_ICON_VICTIM, getDrawable(R.drawable.ic_victim)!!)
    }

    /**
     * Called once the map and the style are completely initialized
     */
    private fun onceMapReady(style: Style) {
        setupMarkerObserver()
        setupHeatmapsObservers(style)
        /**Uncomment this to see a virtual heatmap, if uncommented, tests won't pass**/
        //addVirtualPointsToHeatmap()
    }

    private fun setupMarkerObserver() {
        markerRepository.getMarkersOfSearchGroup(groupId).observe(this, Observer { markers ->
            val removedMarkers = victimMarkers.keys - markers.map { it.uuid }
            removedMarkers.forEach {
                victimSymbolManager.delete(victimMarkers.remove(it))
            }
            markers.filter { !victimMarkers.containsKey(it.uuid) }.forEach {
                addVictimMarker(it.location!!, it.uuid!!)
            }
        })
    }

    /**
     * Instantiates the heatmaps observers:
     *  - An observer for the collection of heatmaps
     *  - An observer for each heatmap for new points
     */
    private fun setupHeatmapsObservers(style: Style) {
        val upperLayerId = victimSymbolManager.layerId
        heatmapRepository.getGroupHeatmaps(groupId).observe(this, Observer { repoHeatmaps ->
            // Observers for heatmap creation
            repoHeatmaps.filter { !heatmapPainters.containsKey(it.key) }
                    .forEach { (key, value) ->
                        heatmapPainters[key] = MapboxHeatmapPainter(style, this, value, upperLayerId)
                    }

            // Remove observers on heatmap deletion
            val removedHeatmapIds = heatmapPainters.keys - repoHeatmaps.keys
            removedHeatmapIds.forEach {
                heatmapPainters[it]!!.destroy(mapboxMap.style!!)
                heatmapPainters.remove(it)
            }
        })
    }

    /**
     * Updates the text of the given textView with the given value and format, or the default string
     * if the value is null
     */
    private fun updateTextView(textView: TextView, value: Double?, formatString: String) {
        textView.text = value?.let { formatString.format(it) } ?: getString(R.string.no_info)
    }

    fun onMapClicked(position: LatLng) {
        if (role == Role.OPERATOR) {
            try {
                searchAreaBuilder.addVertex(position)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onMapLongClicked(position: LatLng) {
        if (!victimSymbolLongClickConsumed) {
            markerRepository.addMarkerForSearchGroup(groupId, position)
        }
        victimSymbolLongClickConsumed = false
    }

    /**
     * Adds a heat point to the heatmap
     */
    fun addPointToHeatMap(location: LatLng, intensity: Double) {
        if (isMapReady) {
            heatmapRepository.addMeasureToHeatmap(groupId, Auth.accountId.value!!, location, intensity)
        }
    }

    /**
     * Centers the camera on the drone
     */
    fun centerCameraOnDrone(v: View) {
        val currentZoom = mapboxMap.cameraPosition.zoom
        if (Drone.currentPositionLiveData.value != null) {
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Drone.currentPositionLiveData.value!!,
                    if (currentZoom > DEFAULT_ZOOM - ZOOM_TOLERANCE && currentZoom < DEFAULT_ZOOM + ZOOM_TOLERANCE) currentZoom else DEFAULT_ZOOM))
        }
    }

    fun startMissionOrReturnHome(v: View) {
        if (!Drone.isConnected()) {
            snackbar.show()
        }

        if (!Drone.isFlying()) { //TODO : return to user else
            Drone.startMission(DroneUtils.makeDroneMission(missionBuilder.build()))
        }
        findViewById<FloatingActionButton>(R.id.start_or_return_button)
                .setIcon(if (Drone.isFlying()) R.drawable.ic_return else R.drawable.ic_start)
    }

    fun storeMap(v: View) {
        startActivity(Intent(applicationContext, OfflineManagerActivity::class.java))
    }

    /**
     * Clears the waypoints list and removes all the lines and points related to waypoints
     */
    fun clearWaypoints(v: View) {
        if (isMapReady) {
            searchAreaBuilder.reset()
        }
    }

    private fun addVictimMarker(latLng: LatLng, markerId: String) {
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
        CentralLocationManager.currentUserPosition.value.let {
            updateTextView(distanceToUserTextView, it?.distanceTo(newLatLng), DISTANCE_FORMAT)
        }
    }

    /**
     * Updates the user position if the drawing managers are ready
     */
    private fun updateUserPosition(userLatLng: LatLng) {
        Drone.currentPositionLiveData.value?.let {
            updateTextView(distanceToUserTextView, it.distanceTo(userLatLng), DISTANCE_FORMAT)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun pickStrategy(view: View) {
        if (currentStrategy is SimpleMultiPassOnQuadrilateral) {
            setStrategy(SpiralStrategy(Drone.GROUND_SENSOR_SCOPE))
        } else {
            setStrategy(SimpleMultiPassOnQuadrilateral(Drone.GROUND_SENSOR_SCOPE))
        }
    }

    fun setStrategy(strategy: OverflightStrategy) {
        if (isMapReady) {
            searchAreaBuilder.onDestroy()
            searchAreaPainter.onDestroy()
        }

        currentStrategy = strategy
        when (strategy) {
            is SimpleMultiPassOnQuadrilateral -> {
                searchAreaPainter = MapboxQuadrilateralPainter(mapView, mapboxMap, mapboxMap.style!!)
                searchAreaBuilder = QuadrilateralBuilder()
                strategyPickerButton.setIcon(R.drawable.ic_quadstrat)
            }
            is SpiralStrategy -> {
                searchAreaPainter = MapboxCirclePainter(mapView, mapboxMap, mapboxMap.style!!)
                searchAreaBuilder = CircleBuilder()
                strategyPickerButton.setIcon(R.drawable.ic_spiralstrat)
            }
        }

        missionBuilder.withStrategy(currentStrategy)

        searchAreaBuilder.searchAreaChanged.add { missionBuilder.withSearchArea(it) }
        searchAreaBuilder.verticesChanged.add { searchAreaPainter.paint(it) }
        searchAreaPainter.onMoveVertex.add { old, new -> searchAreaBuilder.moveVertex(old, new) }
    }
}