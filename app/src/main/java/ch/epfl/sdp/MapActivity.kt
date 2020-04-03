package ch.epfl.sdp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
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
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.ColorUtils
import java.text.DecimalFormat

/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : MapViewBaseActivity(), OnMapReadyCallback {
    private var mapboxMap: MapboxMap? = null

    private var wayptCircleManager: CircleManager? = null
    private var droneCircleManager: CircleManager? = null
    private var userCircleManager: CircleManager? = null

    private var dronePositionMarker: Circle? = null
    private var userPositionMarker: Circle? = null

    private var lineManager: LineManager? = null
    private var fillManager: FillManager? = null

    var waypoints = arrayListOf<LatLng>()

    private var featureCollection: FeatureCollection? = null
    private var features = ArrayList<Feature>()
    private var geoJsonSource: GeoJsonSource? = null

    private val darkRed = Color.parseColor("#E55E5E")
    private val lightRed = Color.parseColor("#F9886C")
    private val orange = Color.parseColor("#FBB03B")


    private var dronePositionObserver = Observer<LatLng> { newLatLng: LatLng? -> newLatLng?.let { updateVehiclePosition(it) } }
    private var userPositionObserver = Observer<LatLng> { newLatLng: LatLng? -> newLatLng?.let { updateUserPosition(it) } }
    //private var currentMissionPlanObserver = Observer { latLngs: List<LatLng> -> updateMarkers(latLngs) }

    var userLatLng: LatLng = LatLng()
        private set

    companion object {
        private const val MAP_NOT_READY_DESCRIPTION: String = "MAP NOT READY"
        private const val MAP_READY_DESCRIPTION: String = "MAP READY"

        private const val PATH_THICKNESS: Float = 5F
        private const val REGION_FILL_OPACITY: Float = 0.5F
        private const val TEXT_PATTERN = "0.0000000"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        findViewById<Button>(R.id.start_mission_button).setOnClickListener {
            DroneMission.makeDroneMission(Drone.overflightStrategy.createFlightPath(waypoints)).startMission()
        }

        findViewById<Button>(R.id.stored_offline_map).setOnClickListener {
            startActivity(Intent(applicationContext, OfflineManagerActivity::class.java))
        }

        findViewById<Button>(R.id.clear_waypoints).setOnClickListener {
            clearWaypoints()
        }

        mapView.contentDescription = MAP_NOT_READY_DESCRIPTION



        CentralLocationManager.configure(this)

    }

    override fun onResume() {
        super.onResume()
        Drone.currentPositionLiveData.observe(this, dronePositionObserver)
        CentralLocationManager.currentUserPosition.observe(this, userPositionObserver)
        // viewModel.currentMissionPlanLiveData.observe(this, currentMissionPlanObserver)
    }

    override fun onPause() {
        super.onPause()
        CentralLocationManager.currentUserPosition.removeObserver(userPositionObserver)
        Drone.currentPositionLiveData.removeObserver(dronePositionObserver)
        //Mission.currentMissionPlanLiveData.removeObserver(currentMissionPlanObserver)
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.latitude), mapboxMap?.cameraPosition?.target?.latitude.toString())
                .putString(getString(R.string.longitude), mapboxMap?.cameraPosition?.target?.longitude.toString())
                .putString(getString(R.string.zoom), mapboxMap?.cameraPosition?.zoom.toString())
                .apply()
        super.onStop()
        MapUtils.saveCameraPositionAndZoomToPrefs(this, mapboxMap)

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            fillManager = FillManager(mapView, mapboxMap, style)
            lineManager = LineManager(mapView, mapboxMap, style)
            wayptCircleManager = CircleManager(mapView, mapboxMap, style)
            droneCircleManager = CircleManager(mapView, mapboxMap, style)
            userCircleManager = CircleManager(mapView, mapboxMap, style)

            mapboxMap.addOnMapClickListener { position ->
                onMapClicked(position)
                true
            }

            createLayersForHeatMap(style)
            /**THIS IS JUST TO ADD SOME POINTS, IT WILL BE REMOVED AFTERWARDS**/
            addPointToHeatMap(8.543434, 47.398979)
            addPointToHeatMap(8.543934, 47.398279)
            addPointToHeatMap(8.544867, 47.397426)
            addPointToHeatMap(8.543067, 47.397026)
        }

        // Load latest location
        MapUtils.setupCameraAsLastTimeUsed(this, mapboxMap)

        // Used to detect when the map is ready in tests
        mapView.contentDescription = MAP_READY_DESCRIPTION
    }


    /**
     * Update [currentPositionMarker] position with a new [position].
     *
     * @param newLatLng new position of the vehicle
     */
    private fun updateVehiclePosition(newLatLng: LatLng) {
        if (mapboxMap == null || droneCircleManager == null) {
            // Not ready
            return
        }

        // Add a vehicle marker and move the camera
        if (dronePositionMarker == null) {
            val circleOptions = CircleOptions()
            circleOptions.withLatLng(newLatLng)
            circleOptions.withCircleColor(ColorUtils.colorToRgbaString(Color.RED))
            dronePositionMarker = droneCircleManager!!.create(circleOptions)

            mapboxMap!!.moveCamera(CameraUpdateFactory.tiltTo(0.0))
            mapboxMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 14.0))
        } else {
            dronePositionMarker!!.latLng = newLatLng
            droneCircleManager!!.update(dronePositionMarker)
        }

        //display coordinates in the bar
        val df = DecimalFormat(TEXT_PATTERN);

        val latView: TextView = findViewById(R.id.tv_latitude)
        latView.text = (getString(R.string.lat) + df.format(newLatLng.latitude))
        val lonView: TextView = findViewById(R.id.tv_longitude)
        lonView.text = (getString(R.string.lon) + df.format(newLatLng.longitude))
    }

    /** Trajectory Planning **/
    fun onMapClicked(position: LatLng): Boolean{
        if (waypoints.size < pinPointsAmount){
            waypoints.add(position)
            drawPinpoint(position)

            if (waypoints.isNotEmpty()){
                drawRegion(waypoints)
            }

            if (waypoints.size == pinPointsAmount){
                drawPath(Drone.overflightStrategy.createFlightPath(waypoints))
            }
        }
        return true
    }

    private fun drawPath(path: List<LatLng>){
        lineManager?.create(LineOptions()
                .withLatLngs(path)
                .withLineWidth(PATH_THICKNESS))
    }

    private fun drawRegion(corners: List<LatLng>){
        // Draw the fill
        val fillOption = FillOptions()
                .withLatLngs(listOf(waypoints))
                .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                .withFillOpacity(REGION_FILL_OPACITY)
        fillManager?.deleteAll()
        fillManager?.create(fillOption)

        //Draw the borders
        // Make it loop
        val linePoints = arrayListOf<LatLng>().apply {
            addAll(corners)
            add(corners[0])
        }
        val lineOptions = LineOptions()
                .withLatLngs(linePoints)
                .withLineColor(ColorUtils.colorToRgbaString(Color.LTGRAY))
        lineManager?.deleteAll()
        lineManager?.create(lineOptions)
    }

    private fun drawPinpoint(pinpoints: LatLng){
        val circleOptions = CircleOptions()
                .withLatLng(pinpoints)
                .withDraggable(true)
        wayptCircleManager?.create(circleOptions)
    }

    private fun clearWaypoints() {
        //FIXME : DOESN'T WORKS
        waypoints.clear()
        wayptCircleManager?.deleteAll()
        lineManager?.deleteAll()
        fillManager?.deleteAll()

    }

    private fun createLayersForHeatMap(style: Style) {
        createSourceData(style)
        unclusteredLayerData(style)
        clusteredLayerData(style)
    }

    fun addPointToHeatMap(longitude: Double, latitude: Double) {
        features.add(Feature.fromGeometry(Point.fromLngLat(longitude, latitude)))
        featureCollection = FeatureCollection.fromFeatures(features)
        geoJsonSource!!.setGeoJson(featureCollection)

    }

    private fun createSourceData(style: Style) {
        geoJsonSource = GeoJsonSource(getString(R.string.heatmap_source_ID), GeoJsonOptions().withCluster(true))
        geoJsonSource!!.setGeoJson(featureCollection)
        style.addSource(geoJsonSource!!)
    }

    private fun unclusteredLayerData(style: Style) {
        val unclustered = CircleLayer("unclustered-points", getString(R.string.heatmap_source_ID))
        unclustered.setProperties(
                circleColor(orange),
                circleRadius(20f),
                circleBlur(1f))
        unclustered.setFilter(neq(get("cluster"), literal(true)))
        style.addLayerBelow(unclustered, getString(R.string.below_layer_id))
    }

    private fun clusteredLayerData(style: Style) {
        val layers = arrayOf(
                intArrayOf(4, darkRed),
                intArrayOf(2, lightRed),
                intArrayOf(0, orange))
        for (i in layers.indices) {
            val circles = CircleLayer("cluster-$i", getString(R.string.heatmap_source_ID))
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(60f),
                    circleBlur(1f)
            )
            val pointCount: Expression = toNumber(get("point_count"))
            circles.setFilter(
                    if (i == 0) gte(pointCount, literal(layers[i][0])) else all(
                            gte(pointCount, literal(layers[i][0])),
                            lt(pointCount, literal(layers[i - 1][0]))
                    )
            )
            style.addLayerBelow(circles, getString(R.string.below_layer_id))
        }
    }

    private fun updateUserPosition(userLatLng: LatLng) {
        if (mapboxMap == null || userCircleManager == null) {
            // Not ready
            return
        }

        // Add a vehicle marker and move the camera
        if (userPositionMarker == null) {
            val circleOptions = CircleOptions()
            circleOptions.withLatLng(userLatLng)
            userPositionMarker = userCircleManager!!.create(circleOptions)
        } else {
            userPositionMarker!!.latLng = userLatLng
            userCircleManager!!.update(userPositionMarker)
        }
    }

//    /**
//     * Update the [map] with the current mission plan waypoints.
//     *
//     * @param latLngs current mission waypoints
//     */
//    private fun updateMarkers(latLngs: List<LatLng>) {
//        if (circleManager != null) {
//            circleManager!!.delete(waypoints)
//            waypoints.clear()
//        }
//        for (latLng in latLngs) {
//            val circleOptions: CircleOptions = CircleOptions()
//                    .withLatLng(latLng)
//                    .withCircleColor(ColorUtils.colorToRgbaString(Color.BLUE))
//                    .withCircleStrokeColor(ColorUtils.colorToRgbaString(Color.BLACK))
//                    .withCircleStrokeWidth(1.0f)
//                    .withCircleRadius(12f)
//                    .withDraggable(false)
//            circleManager?.create(circleOptions)
//        }
//    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
