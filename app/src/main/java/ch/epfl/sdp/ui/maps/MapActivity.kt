package ch.epfl.sdp.ui.maps

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data_manager.HeatmapDataManager
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

/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : MapViewBaseActivity(), OnMapReadyCallback, MapboxMap.OnMapLongClickListener, MapboxMap.OnMapClickListener {

    private lateinit var groupId: String
    private var isMapReady = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var isCameraFragmentFullScreen = true

    private lateinit var mapboxMap: MapboxMap

    private lateinit var role: Role
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
    private lateinit var searchAreaPainter: SearchAreaPainter
    private lateinit var missionPainter: MapboxMissionPainter
    private lateinit var dronePainter: MapboxDronePainter

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var victimSymbolManager: VictimSymbolManager

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var measureHeatmapManager: MeasureHeatmapManager

    private var dronePositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let { if (::dronePainter.isInitialized) dronePainter.paint(it) }
    }

    companion object {
        private const val SCALE_FACTOR = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requireNotNull(intent.getStringExtra(getString(R.string.intent_key_group_id))) { "MapActivity should be provided with a searchGroupId\n" }
        require(Auth.loggedIn.value == true) { "You need to be logged in to access MapActivity" }
        requireNotNull(Auth.accountId.value) { "You need to have an account ID set to access MapActivity" }
        requireNotNull(intent.getSerializableExtra(getString(R.string.intent_key_role))) { "MapActivity should be provided with a role" }

        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        groupId = intent.getStringExtra(getString(R.string.intent_key_group_id))!!
        role = intent.getSerializableExtra(getString(R.string.intent_key_role)) as Role

        //TODO: Give user location if current drone position is not available
        CentralLocationManager.configure(this)
        mapView.contentDescription = getString(R.string.map_not_ready)

        resizeCameraFragment(mapView)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        if (role == Role.RESCUER) {
            hideOperatorUiComponents()
        }

        //Change button color if the drone is not connected
        if (!Drone.isConnected()) {
            findViewById<FloatingActionButton>(R.id.start_or_return_button).colorNormal = Color.GRAY
        }
    }

    private fun hideOperatorUiComponents() {
        findViewById<FloatingActionButton>(R.id.start_or_return_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.clear_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.locate_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.strategy_picker_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.strategy_picker_button)!!.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.resize_button)!!.visibility = View.GONE
        findViewById<ConstraintLayout>(R.id.vlc_fragment)!!.visibility = View.GONE
        findViewById<TableLayout>(R.id.drone_status_fragment)!!.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        Drone.currentPositionLiveData.observe(this, dronePositionObserver)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        Drone.currentPositionLiveData.removeObserver(dronePositionObserver)

        if (isMapReady) MapUtils.saveCameraPositionAndZoomToPrefs(mapboxMap.cameraPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMapReady) {
            dronePainter.onDestroy()
            missionPainter.onDestroy()
            victimSymbolManager.onDestroy()
            measureHeatmapManager.onDestroy()
            searchAreaPainter.onDestroy()
        }

        CentralLocationManager.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            dronePainter = MapboxDronePainter(mapView, mapboxMap, style)
            victimSymbolManager = VictimSymbolManager(mapView, mapboxMap, style) { markerId -> markerManager.removeMarkerForSearchGroup(groupId, markerId) }
            measureHeatmapManager = MeasureHeatmapManager(mapView, mapboxMap, style, victimSymbolManager.layerId())
            missionPainter = MapboxMissionPainter(mapView, mapboxMap, style)
            searchAreaPainter = SearchAreaPainter(mapView, mapboxMap, style)

            mapboxMap.addOnMapClickListener(this)
            mapboxMap.addOnMapLongClickListener(this)

            // Load latest location
            mapboxMap.cameraPosition = MapUtils.getLastCameraState()

            //Create builders
            missionBuilder = MissionBuilder().withStartingLocation(LatLng(MapUtils.DEFAULT_LATITUDE, MapUtils.DEFAULT_LONGITUDE))
            setStrategy(loadDefaultStrategyFromPreferences())

            // Configure listeners
            markerManager.getMarkersOfSearchGroup(groupId).observe(this, victimSymbolManager)
            heatmapManager.getGroupHeatmaps(groupId).observe(this, measureHeatmapManager)
            Drone.currentPositionLiveData.observe(this, Observer { missionBuilder.withStartingLocation(it) })
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
        if (role == Role.OPERATOR) {
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
        if (!victimSymbolManager.victimSymbolLongClickConsumed) {
            markerManager.addMarkerForSearchGroup(groupId, position)
        }
        victimSymbolManager.victimSymbolLongClickConsumed = false
        return true
    }

    /**
     * Adds a heat point to the heatmap
     */
    fun addPointToHeatMap(location: LatLng, intensity: Double) {
        if (isMapReady) {
            heatmapManager.addMeasureToHeatmap(groupId, Auth.accountId.value!!, location, intensity)
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
            Toast.makeText(this, getString(R.string.not_connected_message), Toast.LENGTH_SHORT).show()
        } else if (!searchAreaBuilder.isComplete()) { //TODO add missionBuilder isComplete method
            Toast.makeText(this, getString(R.string.not_enough_waypoints_message), Toast.LENGTH_SHORT).show()
        } else if (!Drone.isFlying()) {
            launchMission()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun launchMission() {
        val altitude = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(this.getString(R.string.pref_key_drone_altitude), Drone.DEFAULT_ALTITUDE.toString()).toString().toFloat()
        Drone.startMission(DroneUtils.makeDroneMission(missionBuilder.build(), altitude))

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

    fun resizeCameraFragment(v: View) {
        isCameraFragmentFullScreen = !isCameraFragmentFullScreen

        val size = android.graphics.Point()
        windowManager.defaultDisplay.getSize(size)
        val margin = 2 * resources.getDimension(R.dimen.tiny_margin).toInt()

        //findViewById<Button>(R.id.switch_button).visibility = if(isFragmentBig) View.VISIBLE else View.GONE
        val vlcFragment = findViewById<ConstraintLayout>(R.id.vlc_fragment)
        vlcFragment.layoutParams.width = (if (isCameraFragmentFullScreen) size.x else size.x / SCALE_FACTOR) - margin
        vlcFragment.layoutParams.height = (if (isCameraFragmentFullScreen) size.y else size.y / SCALE_FACTOR) - margin
        vlcFragment.requestLayout()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun pickStrategy(view: View) {
        if (currentStrategy is SimpleQuadStrategy) {
            setStrategy(SpiralStrategy(Drone.GROUND_SENSOR_SCOPE))
        } else {
            setStrategy(SimpleQuadStrategy(Drone.GROUND_SENSOR_SCOPE))
        }
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