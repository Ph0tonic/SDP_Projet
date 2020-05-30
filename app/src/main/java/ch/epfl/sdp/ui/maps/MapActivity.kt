package ch.epfl.sdp.ui.maps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data_manager.HeatmapDataManager
import ch.epfl.sdp.database.data_manager.MainDataManager
import ch.epfl.sdp.database.data_manager.MarkerDataManager
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.DroneUtils
import ch.epfl.sdp.map.*
import ch.epfl.sdp.map.MapUtils.DEFAULT_ZOOM
import ch.epfl.sdp.map.MapUtils.ZOOM_TOLERANCE
import ch.epfl.sdp.mission.MissionBuilder
import ch.epfl.sdp.mission.OverflightStrategy
import ch.epfl.sdp.mission.SimpleQuadStrategy
import ch.epfl.sdp.mission.SpiralStrategy
import ch.epfl.sdp.searcharea.CircleBuilder
import ch.epfl.sdp.searcharea.QuadrilateralBuilder
import ch.epfl.sdp.searcharea.SearchAreaBuilder
import ch.epfl.sdp.ui.drone.ReturnDroneDialogFragment
import ch.epfl.sdp.ui.maps.offline.OfflineManagerActivity
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import ch.epfl.sdp.utils.StrategyUtils.loadDefaultStrategyFromPreferences
import com.getbase.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import io.mavsdk.telemetry.Telemetry

/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : MapViewBaseActivity(), OnMapReadyCallback, MapboxMap.OnMapLongClickListener, MapboxMap.OnMapClickListener {

    private var isMapReady = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var isCameraFragmentFullScreen = true

    private lateinit var mapboxMap: MapboxMap

    // Allow to no trigger long click when the event has already been consumed by a painter
    // Mapbox annotation plugin PR has been merged but no released yet
    private var longClickConsumed = false

    private lateinit var currentStrategy: OverflightStrategy

    /** Builders */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var searchAreaBuilder: SearchAreaBuilder

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var missionBuilder: MissionBuilder

    /* Data managers */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val heatmapManager = HeatmapDataManager()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val markerManager = MarkerDataManager()

    /* Painters */
    private lateinit var searchAreaPainter: MapboxSearchAreaPainter
    private lateinit var missionPainter: MapboxMissionPainter
    private lateinit var dronePainter: MapboxDronePainter
    private lateinit var homePainter: MapboxHomePainter

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var victimSymbolManager: VictimSymbolManager

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var measureHeatmapManager: MeasureHeatmapManager

    private var dronePositionObserver = Observer<LatLng> { newLatLng ->
        newLatLng?.let { if (::dronePainter.isInitialized) dronePainter.paint(it) }
    }

    private var homePositionObserver = Observer<Telemetry.Position> { newPosition: Telemetry.Position? ->
        newPosition?.let {
            if (::homePainter.isInitialized) homePainter.paint(LatLng(it.latitudeDeg, it.longitudeDeg))
        }
    }

    private var droneFlyingStatusObserver = Observer<Boolean> {
        findViewById<FloatingActionButton>(R.id.return_home_or_user)!!.visibility = if (it) View.VISIBLE else View.GONE
    }

    private lateinit var startOrPauseButton: FloatingActionButton
    private var defaultColor: Int = 0
    private var droneConnectionStatusObserver = Observer<Boolean> {
        startOrPauseButton.colorNormal = if (it) defaultColor else startOrPauseButton.colorDisabled
    }

    private var missionStatusObserver = Observer<Boolean> {
        startOrPauseButton.setIcon(if (it) R.drawable.ic_play_arrow_black_24dp else R.drawable.ic_pause_black_24dp)
    }

    companion object {
        private const val SCALE_FACTOR = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        require(!MainDataManager.groupId.value.isNullOrEmpty()) { "MapActivity should be provided with a valid searchGroupId\n" }
        require(Auth.loggedIn.value == true) { "You need to be logged in to access MapActivity" }
        requireNotNull(Auth.accountId.value) { "You need to have an account ID set to access MapActivity" }
        requireNotNull(MainDataManager.role.value) { "MapActivity should be provided with a role" }

        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        //TODO: Give user location if current drone position is not available
        CentralLocationManager.configure(this)
        mapView.contentDescription = getString(R.string.map_not_ready)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        startOrPauseButton = findViewById(R.id.start_or_pause_button)
        defaultColor = startOrPauseButton.colorNormal

        if (MainDataManager.role.value == Role.RESCUER) {
            hideOperatorUiComponents()
        }
    }

    private fun hideOperatorUiComponents() {
        findViewById<FloatingActionButton>(R.id.start_or_pause_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.clear_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.locate_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.strategy_picker_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.strategy_picker_button)!!.visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.vlc_fragment)!!.visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.drone_status_fragment)!!.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        Drone.positionLiveData.observe(this, dronePositionObserver)
        Drone.homeLocationLiveData.observe(this, homePositionObserver)
        Drone.isFlyingLiveData.observe(this, droneFlyingStatusObserver)
        Drone.isConnectedLiveData.observe(this, droneConnectionStatusObserver)
        Drone.isMissionPausedLiveData.observe(this, missionStatusObserver)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        Drone.positionLiveData.removeObserver(dronePositionObserver)
        Drone.homeLocationLiveData.removeObserver(homePositionObserver)
        Drone.isFlyingLiveData.removeObserver(droneFlyingStatusObserver)
        Drone.isConnectedLiveData.removeObserver(droneConnectionStatusObserver)
        Drone.isMissionPausedLiveData.removeObserver(missionStatusObserver)

        if (isMapReady) MapUtils.saveCameraPositionAndZoomToPrefs(mapboxMap.cameraPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMapReady) {
            dronePainter.onDestroy()
            missionPainter.onDestroy()
            homePainter.onDestroy()
            searchAreaPainter.onDestroy()
            victimSymbolManager.onDestroy()
            measureHeatmapManager.onDestroy()
        }

        CentralLocationManager.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            dronePainter = MapboxDronePainter(mapView, mapboxMap, style)
            victimSymbolManager = VictimSymbolManager(mapView, mapboxMap, style, { markerId -> markerManager.removeMarkerForSearchGroup(MainDataManager.groupId.value!!, markerId) }) { longClickConsumed = true }
            homePainter = MapboxHomePainter(mapView, mapboxMap, style)
            measureHeatmapManager = MeasureHeatmapManager(mapView, mapboxMap, style, victimSymbolManager.layerId())
            missionPainter = MapboxMissionPainter(mapView, mapboxMap, style)
            searchAreaPainter = MapboxSearchAreaPainter(mapView, mapboxMap, style) { longClickConsumed = true }

            mapboxMap.addOnMapClickListener(this)
            mapboxMap.addOnMapLongClickListener(this)

            // Load latest location
            mapboxMap.cameraPosition = MapUtils.getLastCameraState()

            //Create builders
            missionBuilder = MissionBuilder().withStartingLocation(LatLng(MapUtils.DEFAULT_LATITUDE, MapUtils.DEFAULT_LONGITUDE))
            setStrategy(loadDefaultStrategyFromPreferences())

            // Configure listeners
            markerManager.getMarkersOfSearchGroup(MainDataManager.groupId.value!!).observe(this, victimSymbolManager)
            heatmapManager.getGroupHeatmaps(MainDataManager.groupId.value!!).observe(this, measureHeatmapManager)
            Drone.positionLiveData.observe(this, Observer { missionBuilder.withStartingLocation(it) })
            missionBuilder.generatedMissionChanged.add { missionPainter.paint(it) }

            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build())
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS

            isMapReady = true

            // Used to detect when the map is ready in tests
            mapView.contentDescription = getString(R.string.map_ready)
        }
    }

    override fun onMapClick(position: LatLng): Boolean {
        if (MainDataManager.role.value == Role.OPERATOR) {
            try {
                searchAreaBuilder.addVertex(position)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onMapLongClick(position: LatLng): Boolean {
        // Need mapbox update to remove this test
        if (!longClickConsumed) {
            markerManager.addMarkerForSearchGroup(MainDataManager.groupId.value!!, position)
        }
        longClickConsumed = false
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Adds a heat point to the heatmap
     */
    fun addPointToHeatMap(location: LatLng, intensity: Double) {
        if (isMapReady) {
            heatmapManager.addMeasureToHeatmap(MainDataManager.groupId.value!!, Auth.accountId.value!!, location, intensity)
        }
    }

    /**
     * Centers the camera on the drone
     */
    fun centerCameraOnDrone(v: View) {
        val currentZoom = mapboxMap.cameraPosition.zoom
        if (Drone.positionLiveData.value != null) {
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Drone.positionLiveData.value!!,
                    if (currentZoom > DEFAULT_ZOOM - ZOOM_TOLERANCE && currentZoom < DEFAULT_ZOOM + ZOOM_TOLERANCE) currentZoom else DEFAULT_ZOOM))
        }
    }

    /**
     * Shows a Toast if the drone is not connected or
     * if there are not enough waypoints for a mission
     * If the drone is on ground -> starts mission
     * If the drone is flying -> shows return dialog
     */
    fun startOrPauseMissionButton(v: View) {
        if (!Drone.isConnectedLiveData.value!!) {
            Toast.makeText(this, getString(R.string.not_connected_message), Toast.LENGTH_SHORT).show()
        } else if (!searchAreaBuilder.isComplete()) { //TODO add missionBuilder isComplete method
            Toast.makeText(this, getString(R.string.not_enough_waypoints_message), Toast.LENGTH_SHORT).show()
        } else {
            launchMission()
        }
    }

    fun returnHomeOrUser(v: View) {
        ReturnDroneDialogFragment().show(supportFragmentManager, this.getString(R.string.ReturnDroneDialogFragment))
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun launchMission() {
        val altitude = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(this.getString(R.string.pref_key_drone_altitude), Drone.DEFAULT_ALTITUDE.toString()).toString().toFloat()
        Drone.startOrPauseMission(DroneUtils.makeDroneMission(missionBuilder.build(), altitude))
    }

    /**
     * Starts OfflineManagerActivity
     */
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

    fun pickStrategy(view: View) {
        if (currentStrategy is SimpleQuadStrategy) {
            setStrategy(SpiralStrategy(Drone.GROUND_SENSOR_SCOPE))
        } else {
            setStrategy(SimpleQuadStrategy(Drone.GROUND_SENSOR_SCOPE))
        }
    }

    fun getStrategy(): OverflightStrategy {
        return currentStrategy
    }

    fun setStrategy(strategy: OverflightStrategy) {
        if (isMapReady) {
            searchAreaBuilder.onDestroy()
        }
        val strategyIcon: Int
        currentStrategy = strategy
        when (strategy) {
            is SimpleQuadStrategy -> {
                searchAreaBuilder = QuadrilateralBuilder()
                strategyIcon = R.drawable.ic_quadstrat
            }
            is SpiralStrategy -> {
                searchAreaBuilder = CircleBuilder()
                strategyIcon = R.drawable.ic_spiralstrat
            }
            else -> throw java.lang.IllegalArgumentException("setStrategy doesn't support the strategy type of: $strategy")
        }
        findViewById<FloatingActionButton>(R.id.strategy_picker_button).setIcon(strategyIcon)


        missionBuilder.withStrategy(currentStrategy)

        searchAreaBuilder.onSearchAreaChanged.add { missionBuilder.withSearchArea(it) }
        searchAreaBuilder.onVerticesChanged.add { searchAreaPainter.paint(searchAreaBuilder) }

        searchAreaPainter.onVertexMoved.add { old, new -> searchAreaBuilder.moveVertex(old, new) }
    }

}