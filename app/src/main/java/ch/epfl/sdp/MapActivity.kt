package ch.epfl.sdp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.DroneUtils
import ch.epfl.sdp.map.MapBoxMissionPainter
import ch.epfl.sdp.map.MapBoxQuadrilateralPainter
import ch.epfl.sdp.map.MapBoxSearchAreaPainter
import ch.epfl.sdp.map.MapUtils
import ch.epfl.sdp.mission.MissionBuilder
import ch.epfl.sdp.mission.SimpleMultiPassOnQuadrilateral
import ch.epfl.sdp.searcharea.QuadrilateralBuilder
import ch.epfl.sdp.searcharea.SearchAreaBuilder
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Circle
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.ColorUtils

/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : MapViewBaseActivity(), OnMapReadyCallback {

    private lateinit var mapboxMap: MapboxMap

    private var isMapReady = false

    private lateinit var droneCircleManager: CircleManager
    private lateinit var userCircleManager: CircleManager

    private lateinit var dronePositionMarker: Circle
    private lateinit var userPositionMarker: Circle

    private lateinit var geoJsonSource: GeoJsonSource
    private var features = ArrayList<Feature>()

    private lateinit var droneBatteryLevelImageView: ImageView
    private lateinit var droneBatteryLevelTextView: TextView
    private lateinit var distanceToUserTextView: TextView
    private lateinit var userLongitudeTextView: TextView
    private lateinit var droneAltitudeTextView: TextView
    private lateinit var userLatitudeTextView: TextView
    private lateinit var droneSpeedTextView: TextView

    /** Builders */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var searchAreaBuilder: SearchAreaBuilder

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var missionBuilder: MissionBuilder

    /** Painters */
    private lateinit var searchAreaPainter: MapBoxSearchAreaPainter
    private lateinit var missionPainter: MapBoxMissionPainter

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
        newLatLng?.let { updateDronePosition(it); updateDronePositionOnMap(it) }
    }
    private var userPositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let { updateUserPosition(it); updateUserPositionOnMap(it) }
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

    companion object {
        const val MAP_NOT_READY_DESCRIPTION: String = "MAP NOT READY"
        const val MAP_READY_DESCRIPTION: String = "MAP READY"

        private const val DISTANCE_FORMAT = " %.1f m"
        private const val PERCENTAGE_FORMAT = " %.0f%%"
        private const val SPEED_FORMAT = " %.1f m/s"
        private const val COORDINATE_FORMAT = " %.7f"
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
            Drone.startMission(DroneUtils.makeDroneMission(
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

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            droneCircleManager = CircleManager(mapView, mapboxMap, style)
            userCircleManager = CircleManager(mapView, mapboxMap, style)
            missionPainter = MapBoxMissionPainter(mapView, mapboxMap, style)
            searchAreaPainter = MapBoxQuadrilateralPainter(mapView, mapboxMap, style)

            mapboxMap.addOnMapClickListener {
                onMapClicked(it)
                true
            }
            mapboxMap.addOnMapLongClickListener {
                onMapLongClicked(it)
                true
            }

            geoJsonSource = GeoJsonSource(getString(R.string.heatmap_source_ID), GeoJsonOptions().withCluster(true))
            geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(emptyList<Feature>()))
            style.addSource(geoJsonSource)

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
        }
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
     * Map long clic to current eventListener
     */
    fun onMapLongClicked(position: LatLng) {}

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
    fun addPointToHeatMap(longitude: Double, latitude: Double) {
        if (!isMapReady) return
        features.add(Feature.fromGeometry(Point.fromLngLat(longitude, latitude)))
        geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(features))
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

    private fun updateDronePositionOnMap(newLatLng: LatLng) {
        if (!isMapReady) return

        // Add a vehicle marker and move the camera
        if (!::dronePositionMarker.isInitialized) {
            val circleOptions = CircleOptions()
                    .withLatLng(newLatLng)
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.RED))
            dronePositionMarker = droneCircleManager.create(circleOptions)

//            mapboxMap.moveCamera(CameraUpdateFactory.tiltTo(0.0))
//            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 14.0))
        } else {
            dronePositionMarker.latLng = newLatLng
            droneCircleManager.update(dronePositionMarker)
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

    private fun updateUserPositionOnMap(userLatLng: LatLng) {
        if (!isMapReady) return

        // Add a vehicle marker and move the camera
        if (!::userPositionMarker.isInitialized) {
            val circleOptions = CircleOptions()
                    .withLatLng(userLatLng)
            userPositionMarker = userCircleManager.create(circleOptions)
        } else {
            userPositionMarker.latLng = userLatLng
            userCircleManager.update(userPositionMarker)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
