package ch.epfl.sdp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.SimpleMultiPassOnQuadrilateral
import ch.epfl.sdp.map.*
import ch.epfl.sdp.ui.maps.MapUtils
import ch.epfl.sdp.ui.maps.MapUtils.DEFAULT_ZOOM
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineManagerActivity
import com.getbase.floatingactionbutton.FloatingActionButton
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
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

    private var isMapReady = false
    private var isDroneFlying = false

    private lateinit var mapboxMap: MapboxMap
    private lateinit var droneCircleManager: CircleManager
    private lateinit var userCircleManager: CircleManager
    private lateinit var victimSymbolManager: SymbolManager

    private lateinit var dronePositionMarker: Circle
    var waypoints = arrayListOf<LatLng>()
    private lateinit var userPositionMarker: Circle

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var heatmapFeatures = ArrayList<Feature>()
    private lateinit var heatmapGeoJsonSource: GeoJsonSource

    private lateinit var droneBatteryLevelImageView: ImageView
    private lateinit var droneBatteryLevelTextView: TextView
    private lateinit var distanceToUserTextView: TextView
    private lateinit var droneAltitudeTextView: TextView
    private lateinit var droneSpeedTextView: TextView

    private var victimSymbolLongClickConsumed = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val victimMarkers = mutableListOf<Symbol>()

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
        updateTextView(droneBatteryLevelTextView, newBatteryLevel?.times(100)?.toDouble(), PERCENTAGE_FORMAT) // Always update the text string
        newBatteryLevel?.let { // Only update the icon if the battery level is not null
            val newBatteryDrawable = droneBatteryLevelDrawables
                    .filter { x -> x.first <= newBatteryLevel.coerceAtLeast(0f) }
                    .maxBy { x -> x.first }!!
                    .second
            droneBatteryLevelImageView.setImageResource(newBatteryDrawable)
            droneBatteryLevelImageView.tag = newBatteryDrawable
        }
    }
    private var droneAltitudeObserver = Observer<Float> { newAltitude: Float? -> updateTextView(droneAltitudeTextView, newAltitude?.toDouble(), DISTANCE_FORMAT) }
    private var droneSpeedObserver = Observer<Float> { newSpeed: Float? -> updateTextView(droneSpeedTextView, newSpeed?.toDouble(), SPEED_FORMAT) }

    companion object {
        const val ID_ICON_VICTIM: String = "airport"

        private const val DISTANCE_FORMAT = " %.1f m"
        private const val PERCENTAGE_FORMAT = " %.0f%%"
        private const val SPEED_FORMAT = " %.1f m/s"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        droneBatteryLevelImageView = findViewById(R.id.battery_level_icon)
        droneBatteryLevelTextView = findViewById(R.id.battery_level)
        droneAltitudeTextView = findViewById(R.id.altitude)
        distanceToUserTextView = findViewById(R.id.distance_to_user)
        droneSpeedTextView = findViewById(R.id.speed)

        mapView.contentDescription = getString(R.string.map_not_ready)

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

            victimSymbolManager = SymbolManager(mapView, mapboxMap, style)
            victimSymbolManager.iconAllowOverlap = true
            victimSymbolManager.symbolSpacing = 0F
            victimSymbolManager.iconIgnorePlacement = true
            victimSymbolManager.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT

            victimSymbolManager.addLongClickListener {
                victimSymbolManager.delete(it)
                victimMarkers.remove(it)
                victimSymbolLongClickConsumed = true
            }

            style.addImage(ID_ICON_VICTIM, getDrawable(R.drawable.ic_victim)!!)
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

            heatmapGeoJsonSource = GeoJsonSource(getString(R.string.heatmap_source_ID), GeoJsonOptions()
                    .withCluster(true)
                    .withClusterProperty("intensities", Expression.literal("+"), Expression.get("intensity"))
                    .withClusterMaxZoom(13)
            )
            heatmapGeoJsonSource.setGeoJson(FeatureCollection.fromFeatures(heatmapFeatures))
            style.addSource(heatmapGeoJsonSource)

            MapUtils.createLayersForHeatMap(style)
            mapboxMap.cameraPosition = MapUtils.getLastCameraState()// Load latest location
            mapView.contentDescription = getString(R.string.map_ready)// Used to detect when the map is ready in tests

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

            /**Uncomment this to see a virtual heatmap, if uncommented, tests won't pass**/
            //addVirtualPointsToHeatmap()
        }
    }

    /**
     * Updates the text of the given textView with the given value and format, or the default string
     * if the value is null
     */
    private fun updateTextView(textView: TextView, value: Double?, formatString: String) {
        textView.text = value?.let { formatString.format(it) } ?: getString(R.string.no_info)
    }

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
        }
        victimSymbolLongClickConsumed = false
    }

    /**
     * Clears the waypoints list and removes all the lines and points related to waypoints
     */
    fun clearWaypoints(v: View) {
        if (!isMapReady) return
        searchAreaBuilder.reset()
    }

    fun startMissionOrReturnHome(v: View) {
        if (!isDroneFlying) { //TODO : return to user else
            isDroneFlying = true
            Drone.startMission(DroneMission.makeDroneMission(
                    missionBuilder.build()
            ).getMissionItems())
        }
        findViewById<FloatingActionButton>(R.id.start_or_return_button)
                .setIcon(if (isDroneFlying) R.drawable.ic_return else R.drawable.ic_start)
    }

    fun storeMap(v: View) {
        startActivity(Intent(applicationContext, OfflineManagerActivity::class.java))
    }

    /**
     * Centers the camera on the drone
     */
    fun centerCameraOnDrone(v: View) {
        if (::dronePositionMarker.isInitialized) {
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dronePositionMarker.latLng, DEFAULT_ZOOM))
        }
    }

    /**
     * Adds a heat point to the heatmap
     */
    fun addPointToHeatMap(longitude: Double, latitude: Double, intensity: Double) {
        if (!isMapReady) return
        val feature: Feature = Feature.fromGeometry(Point.fromLngLat(longitude, latitude))
        feature.addNumberProperty("intensity", intensity)
        heatmapFeatures.add(feature)
        heatmapGeoJsonSource.setGeoJson(FeatureCollection.fromFeatures(heatmapFeatures))
        /* Will be needed when we have the signal of the drone implemented */
        //feature.addNumberProperty("intensity", Drone.getSignalStrength())
    }

    private fun addVictimMarker(latLng: LatLng) {
        if (!isMapReady) return
        val symbolOptions = SymbolOptions()
                .withLatLng(LatLng(latLng))
                .withIconImage(ID_ICON_VICTIM)
        victimMarkers.add(victimSymbolManager.create(symbolOptions))
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
}