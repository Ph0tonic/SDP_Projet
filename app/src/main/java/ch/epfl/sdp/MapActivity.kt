package ch.epfl.sdp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.SimpleMultiPassOnQuadrangle.Constraints.pinPointsAmount
import ch.epfl.sdp.ui.maps.MapUtils
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
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

    private lateinit var waypointCircleManager: CircleManager
    private lateinit var droneCircleManager: CircleManager
    private lateinit var userCircleManager: CircleManager
    private lateinit var victimSymbolManager: SymbolManager

    private lateinit var lineManager: LineManager
    private lateinit var fillManager: FillManager

    private lateinit var dronePositionMarker: Circle
    private lateinit var userPositionMarker: Circle

    var waypoints = arrayListOf<LatLng>()

    private var heatmapFeatures = ArrayList<Feature>()
    private var victimLayerFeatures = ArrayList<Feature>()
    private lateinit var heatmapGeoJsonSource: GeoJsonSource
    private lateinit var victimMarkerGeoJsonSource: GeoJsonSource

    private lateinit var distanceToUserTextView: TextView
    private lateinit var droneBatteryLevelTextView: TextView
    private lateinit var droneAltitudeTextView: TextView
    private lateinit var droneSpeedTextView: TextView

    private lateinit var userLatitudeTextView: TextView
    private lateinit var userLongitudeTextView: TextView

    private var dronePositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let { updateDronePosition(it); updateDronePositionOnMap(it) }
    }
    private var userPositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let { updateUserPosition(it); updateUserPositionOnMap(it) }
    }
    private var droneBatteryObserver = Observer<Float> { newBatteryLevel: Float? ->
        newBatteryLevel?.let { updateTextView(droneBatteryLevelTextView, (it * 100).toDouble(), PERCENTAGE_FORMAT) }
    }
    private var droneAltitudeObserver = Observer<Float> { newAltitude: Float? ->
        newAltitude?.let { updateTextView(droneAltitudeTextView, it.toDouble(), DISTANCE_FORMAT) }
    }
    private var droneSpeedObserver = Observer<Float> { newSpeed: Float? ->
        newSpeed?.let { updateTextView(droneSpeedTextView, it.toDouble(), SPEED_FORMAT) }
    }

    companion object {
        const val MAP_NOT_READY_DESCRIPTION: String = "MAP NOT READY"
        const val MAP_READY_DESCRIPTION: String = "MAP READY"

        const val ID_ICON_AIRPORT: String = "airport"

        private const val PATH_THICKNESS: Float = 5F
        private const val REGION_FILL_OPACITY: Float = 0.5F

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

        findViewById<Button>(R.id.start_mission_button).setOnClickListener {
            DroneMission.makeDroneMission(Drone.overflightStrategy.createFlightPath(waypoints)).startMission()
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
        MapUtils.saveCameraPositionAndZoomToPrefs(mapboxMap)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            fillManager = FillManager(mapView, mapboxMap, style)
            lineManager = LineManager(mapView, mapboxMap, style)
            waypointCircleManager = CircleManager(mapView, mapboxMap, style)
            droneCircleManager = CircleManager(mapView, mapboxMap, style)
            userCircleManager = CircleManager(mapView, mapboxMap, style)
            victimSymbolManager = SymbolManager(mapView, mapboxMap, style)
            victimSymbolManager.iconAllowOverlap = true
            victimSymbolManager.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT

            style.addImage(ID_ICON_AIRPORT,
                    BitmapUtils.getBitmapFromDrawable(getDrawable(R.drawable.ic_person_black_24dp))!!,
                    true)


            mapboxMap.addOnMapClickListener { position ->
                onMapClicked(position)
                true
            }

            mapboxMap.addOnMapLongClickListener { position ->
                onMapLongClicked(position)
                true
            }

            heatmapGeoJsonSource = GeoJsonSource(getString(R.string.heatmap_source_ID), GeoJsonOptions().withCluster(true))
            heatmapGeoJsonSource.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
            style.addSource(heatmapGeoJsonSource)
            MapUtils.createLayersForHeatMap(style)

            victimMarkerGeoJsonSource = GeoJsonSource(getString(R.string.victim_marker_source_id))
            victimMarkerGeoJsonSource.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
            style.addSource(victimMarkerGeoJsonSource)
            MapUtils.createLayerForVictimMarker(style)

            // Load latest location
            mapboxMap.cameraPosition = MapUtils.getLastCameraState()

            // Used to detect when the map is ready in tests
            mapView.contentDescription = MAP_READY_DESCRIPTION

            isMapReady = true

            addVictimMarker(LatLng(47.398164, 8.544618))
        }
    }

    private fun updateTextView(textView: TextView, value: Double, formatString: String) {
        textView.text = formatString.format(value)
    }

    /** Trajectory Planning **/
    fun onMapClicked(position: LatLng) {
        if (waypoints.size < pinPointsAmount) {
            waypoints.add(position)
            drawPinpoint(position)
            drawRegion(waypoints)

            if (waypoints.size == pinPointsAmount) {
                drawPath(Drone.overflightStrategy.createFlightPath(waypoints))
            }
        }
    }

    private fun onMapLongClicked(position: LatLng) {
        addVictimMarker(position)
    }

    /**
     * Draws the path given by the list of positions
     */
    private fun drawPath(path: List<LatLng>) {
        if (!isMapReady) return

        lineManager.create(LineOptions()
                .withLatLngs(path)
                .withLineWidth(PATH_THICKNESS))
    }

    /**
     * Fills the regions described by the list of positions
     */
    private fun drawRegion(corners: List<LatLng>) {
        if (!isMapReady) return

        val fillOption = FillOptions()
                .withLatLngs(listOf(waypoints))
                .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                .withFillOpacity(REGION_FILL_OPACITY)
        fillManager.deleteAll()
        fillManager.create(fillOption)

        //Draw the borders

        // Make it loop
        val linePoints = arrayListOf<LatLng>().apply {
            addAll(corners)
            add(corners[0])
        }
        val lineOptions = LineOptions()
                .withLatLngs(linePoints)
                .withLineColor(ColorUtils.colorToRgbaString(Color.LTGRAY))
        lineManager.deleteAll()
        lineManager.create(lineOptions)
    }

    /**
     * Draws a pinpoint on the map at the given position
     */
    private fun drawPinpoint(pinpoints: LatLng) {
        if (!isMapReady) return

        val circleOptions = CircleOptions()
                .withLatLng(pinpoints)
                .withDraggable(true)
        waypointCircleManager.create(circleOptions)
    }

    /**
     * Clears the waypoints list and removes all the lines and points related to waypoints
     */
    private fun clearWaypoints() {
        if (!isMapReady) return

        waypoints.clear()
        waypointCircleManager.deleteAll()
        lineManager.deleteAll()
        fillManager.deleteAll()
    }

    /**
     * Adds a heat point to the heatmap
     */
    fun addPointToHeatMap(longitude: Double, latitude: Double) {
        if(!isMapReady) return
        heatmapFeatures.add(Feature.fromGeometry(Point.fromLngLat(longitude, latitude)))
        heatmapGeoJsonSource.setGeoJson(FeatureCollection.fromFeatures(heatmapFeatures))
    }

    private fun addVictimMarker(latLng: LatLng) {
        if(!isMapReady) return
        Log.w("----------------","pass")
        val symbolOptions = SymbolOptions()
                .withLatLng(LatLng(latLng))
                .withIconImage(ID_ICON_AIRPORT)
        victimSymbolManager.create(symbolOptions)
    }

    /**
     * Update [currentPositionMarker] position with a new [position].
     *
     * @param newLatLng new position of the vehicle
     */
    private fun updateDronePosition(newLatLng: LatLng) {
        CentralLocationManager.currentUserPosition.value?.let {
            val distToUser = it.distanceTo(newLatLng)
            updateTextView(distanceToUserTextView, distToUser, DISTANCE_FORMAT)
        }
    }

    private fun updateDronePositionOnMap(newLatLng: LatLng) {
        if (!isMapReady) return

        // Add a vehicle marker and move the camera
        if (!::dronePositionMarker.isInitialized) {
            val circleOptions = CircleOptions()
            circleOptions.withLatLng(newLatLng)
            circleOptions.withCircleColor(ColorUtils.colorToRgbaString(Color.RED))
            dronePositionMarker = droneCircleManager.create(circleOptions)

            mapboxMap.moveCamera(CameraUpdateFactory.tiltTo(0.0))
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 14.0))
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
        updateTextView(userLongitudeTextView, userLatLng.longitude, getString(R.string.lat) + COORDINATE_FORMAT)

        Drone.currentPositionLiveData.value?.let {
            val distToUser = it.distanceTo(userLatLng)
            updateTextView(distanceToUserTextView, distToUser, DISTANCE_FORMAT)
        }
    }

    private fun updateUserPositionOnMap(userLatLng: LatLng) {
        if (!isMapReady) return

        // Add a vehicle marker and move the camera
        if (!::userPositionMarker.isInitialized) {
            val circleOptions = CircleOptions()
            circleOptions.withLatLng(userLatLng)
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
